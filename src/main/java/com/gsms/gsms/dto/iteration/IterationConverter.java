package com.gsms.gsms.dto.iteration;

import com.gsms.gsms.domain.entity.Iteration;

public class IterationConverter {

    public static Iteration toEntity(IterationCreateReq req) {
        if (req == null) {
            return null;
        }
        Iteration iteration = new Iteration();
        iteration.setProjectId(req.getProjectId());
        iteration.setName(req.getName());
        iteration.setDescription(req.getDescription());
        iteration.setStatus(req.getStatus());
        iteration.setPlanStartDate(req.getPlanStartDate());
        iteration.setPlanEndDate(req.getPlanEndDate());
        return iteration;
    }

    public static Iteration toEntity(IterationUpdateReq req) {
        if (req == null) {
            return null;
        }
        Iteration iteration = new Iteration();
        iteration.setId(req.getId());
        iteration.setName(req.getName());
        iteration.setDescription(req.getDescription());
        iteration.setStatus(req.getStatus());
        iteration.setPlanStartDate(req.getPlanStartDate());
        iteration.setPlanEndDate(req.getPlanEndDate());
        iteration.setActualStartDate(req.getActualStartDate());
        iteration.setActualEndDate(req.getActualEndDate());
        return iteration;
    }
}
