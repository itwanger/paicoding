package com.github.paicoding.forum.web.admin.rest;

import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.article.service.ArticleSlugMigrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文章URL Slug迁移管理接口
 * 仅管理员可访问
 *
 * @author Claude
 * @date 2025-11-10
 */
@Slf4j
@RestController
@RequestMapping("/admin/article/slug")
public class ArticleSlugMigrationController {

    @Autowired
    private ArticleSlugMigrationService migrationService;

    /**
     * 执行全量slug迁移
     * 访问: /admin/article/slug/migrate
     * 需要管理员权限
     *
     * @return 处理结果
     */
    @Permission(role = UserRole.ADMIN)
    @GetMapping("/migrate")
    public ResVo<String> migrateAllSlugs() {
        try {
            int count = migrationService.migrateAllArticleSlugs();
            String message = String.format("迁移完成! 共处理 %d 篇文章", count);
            log.info(message);
            return ResVo.ok(message);
        } catch (Exception e) {
            log.error("Slug迁移失败", e);
            return ResVo.fail(StatusEnum.UNEXPECT_ERROR, "迁移失败: " + e.getMessage());
        }
    }

    /**
     * 重新生成指定文章的slug
     * 访问: /admin/article/slug/regenerate?articleId=123
     *
     * @param articleId 文章ID
     * @return 处理结果
     */
    @Permission(role = UserRole.ADMIN)
    @GetMapping("/regenerate")
    public ResVo<String> regenerateSlug(@RequestParam Long articleId) {
        try {
            boolean success = migrationService.regenerateSlug(articleId);
            if (success) {
                return ResVo.ok("Slug重新生成成功");
            } else {
                return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "Slug重新生成失败,文章可能不存在或标题为空");
            }
        } catch (Exception e) {
            log.error("Slug重新生成失败", e);
            return ResVo.fail(StatusEnum.UNEXPECT_ERROR, e.getMessage());
        }
    }

    /**
     * 查询需要迁移的文章数量
     * 访问: /admin/article/slug/count
     *
     * @return 数量统计
     */
    @Permission(role = UserRole.ADMIN)
    @GetMapping("/count")
    public ResVo<String> countNeedMigration() {
        try {
            long count = migrationService.countArticlesNeedMigration();
            String message = String.format("有 %d 篇文章需要生成slug", count);
            return ResVo.ok(message);
        } catch (Exception e) {
            log.error("统计失败", e);
            return ResVo.fail(StatusEnum.UNEXPECT_ERROR, "统计失败: " + e.getMessage());
        }
    }
}
