package com.gsms.gsms.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gsms.gsms.model.entity.Attachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 附件Mapper接口
 */
@Mapper
public interface AttachmentMapper extends BaseMapper<Attachment> {

    /**
     * 根据关联对象查询附件列表
     * @param targetType 关联对象类型
     * @param targetId 关联对象ID
     * @return 附件列表
     */
    List<Attachment> selectByTarget(@Param("targetType") String targetType, @Param("targetId") Long targetId);

    /**
     * 根据关联对象查询附件数量
     * @param targetType 关联对象类型
     * @param targetId 关联对象ID
     * @return 附件数量
     */
    int countByTarget(@Param("targetType") String targetType, @Param("targetId") Long targetId);

    /**
     * 逻辑删除附件
     * @param id 附件ID
     * @return 影响行数
     */
    int logicalDelete(@Param("id") Long id);
}
