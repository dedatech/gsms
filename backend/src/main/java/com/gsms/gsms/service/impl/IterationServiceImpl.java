package com.gsms.gsms.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.gsms.gsms.model.entity.Iteration;
import com.gsms.gsms.model.entity.Project;
import com.gsms.gsms.model.enums.errorcode.IterationErrorCode;
import com.gsms.gsms.dto.iteration.IterationQueryReq;
import com.gsms.gsms.dto.iteration.IterationCreateReq;
import com.gsms.gsms.dto.iteration.IterationUpdateReq;
import com.gsms.gsms.dto.iteration.IterationConverter;
import com.gsms.gsms.dto.iteration.IterationInfoResp;
import com.gsms.gsms.infra.common.PageResult;
import com.gsms.gsms.infra.exception.BusinessException;
import com.gsms.gsms.infra.utils.UserContext;
import com.gsms.gsms.repository.IterationMapper;
import com.gsms.gsms.repository.ProjectMapper;
import com.gsms.gsms.service.IterationService;
import com.gsms.gsms.service.CacheService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 迭代服务实现类
 */
@Service
public class IterationServiceImpl implements IterationService {

    private final IterationMapper iterationMapper;
    private final ProjectMapper projectMapper;
    private final CacheService cacheService;

    public IterationServiceImpl(IterationMapper iterationMapper, ProjectMapper projectMapper, CacheService cacheService) {
        this.iterationMapper = iterationMapper;
        this.projectMapper = projectMapper;
        this.cacheService = cacheService;
    }

    @Override
    public IterationInfoResp getById(Long id) {
        Iteration iteration = iterationMapper.selectById(id);
        if (iteration == null) {
            throw new BusinessException(IterationErrorCode.ITERATION_NOT_FOUND);
        }
        IterationInfoResp resp = IterationInfoResp.from(iteration);
        enrichIterationInfoResp(resp);
        return resp;
    }

    @Override
    public PageResult<IterationInfoResp> findAll(IterationQueryReq req) {
        PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<Iteration> list = iterationMapper.selectByCondition(req.getProjectId(), req.getStatus());
        PageInfo<Iteration> pageInfo = new PageInfo<>(list);
        List<IterationInfoResp> respList = IterationInfoResp.from(list);

        // 使用缓存填充创建人、更新人信息
        enrichIterationInfoRespList(respList);

        return PageResult.success(respList, pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IterationInfoResp create(IterationCreateReq createReq) {
        // 校验迭代时间
        validateIterationDates(createReq.getProjectId(), null,
                createReq.getPlanStartDate(), createReq.getPlanEndDate(), null, null);

        // DTO转Entity
        Iteration iteration = IterationConverter.toEntity(createReq);
        Long currentUserId = UserContext.getCurrentUserId();
        iteration.setCreateUserId(currentUserId != null ? currentUserId : 1L);
        iteration.setUpdateUserId(currentUserId != null ? currentUserId : 1L);

        int result = iterationMapper.insert(iteration);
        if (result <= 0) {
            throw new BusinessException(IterationErrorCode.ITERATION_CREATE_FAILED);
        }

        return IterationInfoResp.from(iteration);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IterationInfoResp update(IterationUpdateReq updateReq) {
        // 检查迭代是否存在
        Iteration existIteration = iterationMapper.selectById(updateReq.getId());
        if (existIteration == null) {
            throw new BusinessException(IterationErrorCode.ITERATION_NOT_FOUND);
        }

        // 获取最终的时间值（传入的值或现有值）
        LocalDate planStartDate = updateReq.getPlanStartDate() != null ? updateReq.getPlanStartDate() : existIteration.getPlanStartDate();
        LocalDate planEndDate = updateReq.getPlanEndDate() != null ? updateReq.getPlanEndDate() : existIteration.getPlanEndDate();
        LocalDate actualStartDate = updateReq.getActualStartDate() != null ? updateReq.getActualStartDate() : existIteration.getActualStartDate();
        LocalDate actualEndDate = updateReq.getActualEndDate() != null ? updateReq.getActualEndDate() : existIteration.getActualEndDate();

        // 校验迭代时间
        validateIterationDates(existIteration.getProjectId(), existIteration.getId(),
                planStartDate, planEndDate, actualStartDate, actualEndDate);

        // DTO转Entity
        Iteration iteration = IterationConverter.toEntity(updateReq);

        Long currentUserId = UserContext.getCurrentUserId();
        iteration.setUpdateUserId(currentUserId != null ? currentUserId : 1L);

        int result = iterationMapper.update(iteration);
        if (result <= 0) {
            throw new BusinessException(IterationErrorCode.ITERATION_UPDATE_FAILED);
        }

        Iteration updatedIteration = iterationMapper.selectById(iteration.getId());
        return IterationInfoResp.from(updatedIteration);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Iteration existIteration = iterationMapper.selectById(id);
        if (existIteration == null) {
            throw new BusinessException(IterationErrorCode.ITERATION_NOT_FOUND);
        }

        int result = iterationMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException(IterationErrorCode.ITERATION_DELETE_FAILED);
        }
    }

    // ========== 内部方法：数据填充 ==========

    /**
     * 校验迭代日期时间范围
     * 规则：
     * 1. 计划开始时间 <= 计划结束时间（如果结束时间不为空）
     * 2. 实际开始时间 <= 实际结束时间（如果结束时间不为空）
     * 3. 迭代时间必须在项目时间范围内
     * 4. 同一项目的迭代时间不能重合
     *
     * @param projectId 项目ID
     * @param iterationId 迭代ID（更新时传入，创建时传null）
     * @param planStartDate 计划开始时间
     * @param planEndDate 计划结束时间
     * @param actualStartDate 实际开始时间
     * @param actualEndDate 实际结束时间
     */
    private void validateIterationDates(Long projectId, Long iterationId,
                                        LocalDate planStartDate, LocalDate planEndDate,
                                        LocalDate actualStartDate, LocalDate actualEndDate) {
        // 1. 校验计划时间：开始时间不能大于结束时间
        if (planStartDate != null && planEndDate != null) {
            if (planStartDate.isAfter(planEndDate)) {
                throw new BusinessException(IterationErrorCode.ITERATION_DATE_INVALID);
            }
        }

        // 2. 校验实际时间：开始时间不能大于结束时间
        if (actualStartDate != null && actualEndDate != null) {
            if (actualStartDate.isAfter(actualEndDate)) {
                throw new BusinessException(IterationErrorCode.ITERATION_DATE_INVALID);
            }
        }

        // 3. 校验项目时间范围
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(IterationErrorCode.ITERATION_CREATE_FAILED);
        }

        // 迭代计划开始时间不能早于项目计划开始时间
        if (planStartDate != null && project.getPlanStartDate() != null) {
            if (planStartDate.isBefore(project.getPlanStartDate())) {
                throw new BusinessException(IterationErrorCode.ITERATION_DATE_OUT_OF_PROJECT_RANGE);
            }
        }

        // 迭代计划结束时间不能晚于项目计划结束时间
        if (planEndDate != null && project.getPlanEndDate() != null) {
            if (planEndDate.isAfter(project.getPlanEndDate())) {
                throw new BusinessException(IterationErrorCode.ITERATION_DATE_OUT_OF_PROJECT_RANGE);
            }
        }

        // 4. 校验迭代时间重合（只校验计划时间）
        if (planStartDate != null && planEndDate != null) {
            List<Iteration> existingIterations = iterationMapper.selectByProjectId(projectId);
            for (Iteration existing : existingIterations) {
                // 跳过自己（更新时）
                if (iterationId != null && existing.getId().equals(iterationId)) {
                    continue;
                }

                // 跳过没有设置计划时间的迭代
                if (existing.getPlanStartDate() == null || existing.getPlanEndDate() == null) {
                    continue;
                }

                // 判断时间是否重合
                boolean isOverlap = !planEndDate.isBefore(existing.getPlanStartDate())
                        && !planStartDate.isAfter(existing.getPlanEndDate());

                if (isOverlap) {
                    throw new BusinessException(IterationErrorCode.ITERATION_DATE_OVERLAP);
                }
            }
        }
    }

    /**
     * 填充单个 IterationInfoResp 的创建人、更新人信息
     */
    private void enrichIterationInfoResp(IterationInfoResp resp) {
        if (resp.getCreateUserId() != null) {
            String creatorName = cacheService.getUserNicknameById(resp.getCreateUserId());
            resp.setCreateUserName(creatorName);
        }
        if (resp.getUpdateUserId() != null) {
            String updaterName = cacheService.getUserNicknameById(resp.getUpdateUserId());
            resp.setUpdateUserName(updaterName);
        }
    }

    /**
     * 批量填充 IterationInfoResp 列表的创建人、更新人信息
     */
    private void enrichIterationInfoRespList(List<IterationInfoResp> respList) {
        if (respList == null || respList.isEmpty()) {
            return;
        }
        for (IterationInfoResp resp : respList) {
            enrichIterationInfoResp(resp);
        }
    }
}
