package com.gsms.gsms.service;

import com.gsms.gsms.domain.entity.Iteration;

import java.util.List;

public interface IterationService {
    Iteration getIterationById(Long id);

    List<Iteration> getIterationsByProjectId(Long projectId);

    List<Iteration> getIterationsByCondition(Long projectId, Integer status);

    Iteration createIteration(Iteration iteration);

    Iteration updateIteration(Iteration iteration);

    void deleteIteration(Long id);
}
