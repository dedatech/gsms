package com.gsms.gsms.controller;

import com.gsms.gsms.infra.common.Result;
import com.gsms.gsms.service.storage.StorageMigrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 存储迁移控制器（仅用于迁移期间，完成后可删除）
 */
@RestController
@RequestMapping("/api/admin/storage-migration")
public class StorageMigrationController {

    @Autowired
    private StorageMigrationService storageMigrationService;

    /**
     * 迁移所有本地附件到 RustFS
     *
     * 注意：此接口仅用于一次性迁移，迁移完成后应删除或禁用
     */
    @PostMapping("/migrate-to-rustfs")
    public Result<StorageMigrationService.MigrationResult> migrateToRustFS() {
        try {
            StorageMigrationService.MigrationResult result = storageMigrationService.migrateAllToRustFS();
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(500, "迁移失败: " + e.getMessage());
        }
    }
}
