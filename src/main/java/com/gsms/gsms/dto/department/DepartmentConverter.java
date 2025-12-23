package com.gsms.gsms.dto.department;

import com.gsms.gsms.domain.entity.Department;

public class DepartmentConverter {

    public static Department toEntity(DepartmentCreateReq req) {
        if (req == null) {
            return null;
        }
        Department department = new Department();
        department.setName(req.getName());
        department.setParentId(req.getParentId());
        department.setLevel(req.getLevel());
        department.setSort(req.getSort());
        department.setRemark(req.getRemark());
        return department;
    }

    public static Department toEntity(DepartmentUpdateReq req) {
        if (req == null) {
            return null;
        }
        Department department = new Department();
        department.setId(req.getId());
        department.setName(req.getName());
        department.setParentId(req.getParentId());
        department.setLevel(req.getLevel());
        department.setSort(req.getSort());
        department.setRemark(req.getRemark());
        return department;
    }
}
