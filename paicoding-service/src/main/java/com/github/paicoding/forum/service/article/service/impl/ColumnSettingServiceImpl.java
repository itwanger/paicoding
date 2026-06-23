package com.github.paicoding.forum.service.article.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.paicoding.forum.api.model.enums.YesOrNoEnum;
import com.github.paicoding.forum.api.model.enums.column.MovePositionEnum;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.article.*;
import com.github.paicoding.forum.api.model.vo.article.dto.ColumnArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.ColumnArticleGroupDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.ColumnDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.ColumnReadmeDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.SimpleColumnDTO;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.core.util.NumUtil;
import com.github.paicoding.forum.core.util.UrlSlugUtil;
import com.github.paicoding.forum.service.article.conveter.ColumnArticleStructMapper;
import com.github.paicoding.forum.service.article.conveter.ColumnStructMapper;
import com.github.paicoding.forum.service.article.helper.TreeBuilder;
import com.github.paicoding.forum.service.article.repository.dao.ArticleDao;
import com.github.paicoding.forum.service.article.repository.dao.ColumnArticleDao;
import com.github.paicoding.forum.service.article.repository.dao.ColumnArticleGroupDao;
import com.github.paicoding.forum.service.article.repository.dao.ColumnDao;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDetailDO;
import com.github.paicoding.forum.service.article.repository.entity.ColumnArticleDO;
import com.github.paicoding.forum.service.article.repository.entity.ColumnArticleGroupDO;
import com.github.paicoding.forum.service.article.repository.entity.ColumnInfoDO;
import com.github.paicoding.forum.service.article.repository.params.SearchColumnArticleParams;
import com.github.paicoding.forum.service.article.repository.params.SearchColumnParams;
import com.github.paicoding.forum.service.article.service.ColumnSettingService;
import com.github.paicoding.forum.service.article.service.SlugGeneratorService;
import com.github.paicoding.forum.service.chatai.service.impl.zhipu.ZhipuIntegration;
import com.github.paicoding.forum.service.user.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.Asserts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 专栏后台接口
 *
 * @author louzai
 * @date 2022-09-19
 */
@Service
public class ColumnSettingServiceImpl implements ColumnSettingService {
    private static final int README_INIT_SOURCE_MAX_LENGTH = 5000;

    @Autowired
    private UserService userService;

    @Autowired
    private ColumnArticleDao columnArticleDao;

    @Autowired
    private ColumnArticleGroupDao columnArticleGroupDao;

    @Autowired
    private ColumnDao columnDao;

    @Autowired
    private ArticleDao articleDao;

    @Autowired
    private ColumnStructMapper columnStructMapper;

    @Autowired
    private SlugGeneratorService slugGeneratorService;

    @Autowired(required = false)
    private ZhipuIntegration zhipuIntegration;

    @Override
    public void saveColumn(ColumnReq req) {
        ColumnInfoDO columnInfoDO = columnStructMapper.toDo(req);
        boolean isNewColumn = NumUtil.nullOrZero(req.getColumnId());
        ColumnInfoDO oldColumn = isNewColumn ? null : columnDao.getById(req.getColumnId());
        columnInfoDO.setUrlSlug(resolveColumnUrlSlug(req, oldColumn));
        if (req.getFreeStartTime() <= 0) {
            // 兼容日期数据
            columnInfoDO.setFreeStartTime(new Date(1000));
        }
        if (req.getFreeEndTime() <= 0) {
            columnInfoDO.setFreeEndTime(new Date(1000));
        }

        if (isNewColumn) {
            columnDao.save(columnInfoDO);
        } else {
            columnInfoDO.setId(req.getColumnId());

            // 查询修改前的专栏状态
            boolean wasOffline = oldColumn != null && oldColumn.getState() == 0;
            boolean isNowOnline = columnInfoDO.getState() != null && columnInfoDO.getState() != 0;
            
            columnDao.updateById(columnInfoDO);
            
            // 如果专栏从"未发布"变为"连载"或"已完结"，批量发布专栏内的未发布文章
            if (wasOffline && isNowOnline) {
                batchPublishColumnArticles(req.getColumnId());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ColumnReadmeDTO getOrCreateColumnReadme(Long columnId) {
        ColumnInfoDO column = queryColumnForReadme(columnId);
        boolean created = StringUtils.isBlank(column.getReadmeContent());
        return buildReadmeDTO(column, created);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ColumnReadmeDTO saveColumnReadme(ColumnReadmeContentReq req) {
        if (req == null || NumUtil.nullOrZero(req.getColumnId())) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "教程ID不能为空!");
        }

        ColumnInfoDO update = new ColumnInfoDO();
        update.setId(req.getColumnId());
        update.setReadmeContent(StringUtils.defaultString(req.getContent()));
        columnDao.updateById(update);

        return getOrCreateColumnReadme(req.getColumnId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ColumnReadmeDTO initColumnReadmeDraft(Long columnId) {
        ColumnInfoDO column = queryColumnForReadme(columnId);

        ColumnArticleDO firstArticle = columnArticleDao.lambdaQuery()
                .eq(ColumnArticleDO::getColumnId, columnId)
                .orderByAsc(ColumnArticleDO::getSection)
                .last("limit 1")
                .one();
        if (firstArticle == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "当前教程还没有文章，无法初始化说明页!");
        }

        ArticleDO article = articleDao.getById(firstArticle.getArticleId());
        if (article == null) {
            throw ExceptionUtil.of(StatusEnum.ARTICLE_NOT_EXISTS, firstArticle.getArticleId());
        }

        String source = readArticleContent(firstArticle.getArticleId());
        String draft = generateReadmeDraft(column, article, source);
        ColumnReadmeDTO dto = buildReadmeDTO(column, false);
        dto.setContent(draft);
        return dto;
    }

    private ColumnInfoDO queryColumnForReadme(Long columnId) {
        if (NumUtil.nullOrZero(columnId)) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "教程ID不能为空!");
        }
        ColumnInfoDO column = columnDao.getById(columnId);
        if (column == null) {
            throw ExceptionUtil.of(StatusEnum.COLUMN_NOT_EXISTS, columnId);
        }
        return column;
    }

    private String buildDefaultReadmeContent(ColumnInfoDO column) {
        StringBuilder builder = new StringBuilder();
        builder.append("# ").append(column.getColumnName()).append("项目介绍\n\n");
        if (StringUtils.isNotBlank(column.getIntroduction())) {
            builder.append(column.getIntroduction()).append("\n\n");
        }
        builder.append("## 你能学到什么\n\n");
        builder.append("这套教程围绕真实项目展开，覆盖项目背景、业务拆解、架构设计、核心技术实现、部署运行、简历写法和面试复盘。");
        return builder.toString();
    }

    private ColumnReadmeDTO buildReadmeDTO(ColumnInfoDO column, boolean created) {
        ColumnReadmeDTO dto = new ColumnReadmeDTO();
        dto.setColumnId(column.getId());
        dto.setTitle(column.getColumnName() + "项目介绍");
        dto.setContent(StringUtils.defaultIfBlank(column.getReadmeContent(), buildDefaultReadmeContent(column)));
        dto.setCreated(created);
        return dto;
    }

    private String readArticleContent(Long articleId) {
        try {
            ArticleDetailDO detail = articleDao.findLatestDetail(articleId);
            return detail == null ? "" : StringUtils.defaultString(detail.getContent());
        } catch (Exception e) {
            return "";
        }
    }

    private String generateReadmeDraft(ColumnInfoDO column, ArticleDO firstArticle, String sourceContent) {
        String fallback = buildReadmeDraftFallback(column, firstArticle, sourceContent);
        String prompt = buildReadmeInitPrompt(column, firstArticle, sourceContent);
        String answer = callReadmeDraftLLM(prompt);
        if (StringUtils.isBlank(answer)) {
            return fallback;
        }
        String markdown = cleanMarkdownAnswer(answer);
        return StringUtils.isBlank(markdown) ? fallback : markdown;
    }

    private String buildReadmeInitPrompt(ColumnInfoDO column, ArticleDO firstArticle, String sourceContent) {
        String content = StringUtils.defaultString(sourceContent);
        if (content.length() > README_INIT_SOURCE_MAX_LENGTH) {
            content = content.substring(0, README_INIT_SOURCE_MAX_LENGTH);
        }
        return "你是技术教程运营编辑。请根据教程第一篇文章，为教程落地页生成一份可直接编辑的 README Markdown 草稿。\n"
                + "教程名：" + column.getColumnName() + "\n"
                + "教程简介：" + StringUtils.defaultString(column.getIntroduction()) + "\n"
                + "第一篇文章标题：" + StringUtils.defaultString(firstArticle.getTitle()) + "\n"
                + "第一篇文章正文：\n" + content + "\n\n"
                + "要求：\n"
                + "1. 输出 Markdown，不要代码块包裹，不要解释说明。\n"
                + "2. 包含项目介绍、适合人群、技术栈、能学到什么、学习建议这几个部分。\n"
                + "3. 可以保留正文中的 1 张与项目界面或架构相关的图片 Markdown。\n"
                + "4. 不要输出二维码、联系方式、优惠、版权、推荐阅读等营销信息。\n"
                + "5. 控制在 600 到 1000 字，适合 SEO 收录。";
    }

    private String callReadmeDraftLLM(String prompt) {
        try {
            if (zhipuIntegration != null) {
                ChatItemVo item = new ChatItemVo().initQuestion(prompt);
                if (zhipuIntegration.directReturn(0L, item) && StringUtils.isNotBlank(item.getAnswer())) {
                    return item.getAnswer();
                }
            }
        } catch (Exception e) {
            return "";
        }
        return "";
    }

    private String cleanMarkdownAnswer(String answer) {
        String markdown = StringUtils.defaultString(answer).trim();
        if (markdown.startsWith("```")) {
            markdown = markdown.replaceFirst("^```[a-zA-Z]*\\s*", "");
            markdown = markdown.replaceFirst("\\s*```$", "");
        }
        return markdown.trim();
    }

    private String buildReadmeDraftFallback(ColumnInfoDO column, ArticleDO firstArticle, String sourceContent) {
        StringBuilder builder = new StringBuilder();
        builder.append("# ").append(column.getColumnName()).append("项目介绍\n\n");
        if (StringUtils.isNotBlank(column.getIntroduction())) {
            builder.append(column.getIntroduction()).append("\n\n");
        }
        appendFirstImage(builder, sourceContent);
        builder.append("## 项目背景\n\n");
        builder.append(pickFirstParagraph(sourceContent, "这套教程围绕真实项目展开，帮助你从项目背景、业务拆解、技术方案到核心实现完整学习。")).append("\n\n");
        builder.append("## 你能学到什么\n\n");
        builder.append("- 项目的业务场景、核心功能和整体架构\n");
        builder.append("- 关键技术栈的选型原因和落地方式\n");
        builder.append("- 从第一篇「").append(StringUtils.defaultString(firstArticle.getShortTitle(), firstArticle.getTitle())).append("」开始建立项目全局认知\n");
        builder.append("- 可写进简历、可用于面试表达的项目经验\n\n");
        builder.append("## 学习建议\n\n");
        builder.append("建议先阅读项目介绍和环境准备，再按目录顺序完成核心模块学习，最后结合简历写法和面试题进行复盘。");
        return builder.toString();
    }

    private void appendFirstImage(StringBuilder builder, String sourceContent) {
        if (StringUtils.isBlank(sourceContent)) {
            return;
        }
        String[] lines = sourceContent.split("\\r?\\n");
        for (String line : lines) {
            String trim = StringUtils.trimToEmpty(line);
            if (trim.startsWith("![") && !containsLowValueMediaKeyword(trim)) {
                builder.append(trim).append("\n\n");
                return;
            }
        }
    }

    private boolean containsLowValueMediaKeyword(String line) {
        String compact = line.replaceAll("\\s+", "");
        return compact.contains("二维码")
                || compact.contains("扫码")
                || compact.contains("公众号")
                || compact.contains("知识星球")
                || compact.contains("喜报");
    }

    private String pickFirstParagraph(String sourceContent, String fallback) {
        if (StringUtils.isBlank(sourceContent)) {
            return fallback;
        }
        String[] lines = sourceContent.split("\\r?\\n");
        for (String line : lines) {
            String text = StringUtils.trimToEmpty(line)
                    .replaceAll("!\\[[^]]*]\\([^)]*\\)", "")
                    .replaceAll("\\[([^]]+)]\\([^)]*\\)", "$1")
                    .replaceAll("[`*_#>~-]", "")
                    .trim();
            if (text.length() >= 30 && !containsLowValueMediaKeyword(text)) {
                return text.length() > 220 ? text.substring(0, 220) + "…" : text;
            }
        }
        return fallback;
    }

    @Override
    public String generateUrlSlug(AiSlugGenerateReq req) {
        String titleForSlug = StringUtils.defaultIfBlank(req.getShortTitle(), req.getTitle());
        if (StringUtils.isBlank(titleForSlug)) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "教程名不能为空!");
        }
        String slug = slugGeneratorService.generateSlugWithAI(titleForSlug);
        if (StringUtils.isNumeric(slug)) {
            return "column-" + slug;
        }
        return slug;
    }

    private String resolveColumnUrlSlug(ColumnReq req, ColumnInfoDO oldColumn) {
        String inputSlug = StringUtils.isNotBlank(req.getUrlSlug()) ? UrlSlugUtil.generateSlug(req.getUrlSlug()) : null;
        if (isValidColumnSlug(inputSlug)) {
            return generateUniqueColumnSlug(inputSlug, req.getColumnId(), true);
        }
        if (oldColumn != null && isValidColumnSlug(oldColumn.getUrlSlug())) {
            return oldColumn.getUrlSlug();
        }
        return generateUniqueColumnSlug(UrlSlugUtil.generateSlug(req.getColumn()), req.getColumnId(), false);
    }

    private String generateUniqueColumnSlug(String baseSlug, Long columnId, boolean allowArticleConflict) {
        if (StringUtils.isBlank(baseSlug)) {
            baseSlug = UrlSlugUtil.generateSlug("column");
        }
        if (StringUtils.isNumeric(baseSlug)) {
            baseSlug = "column-" + baseSlug;
        }
        String slug = baseSlug;
        int suffix = 2;
        while (columnDao.existsUrlSlug(slug, columnId) || (!allowArticleConflict && articleDao.existsUrlSlug(slug, null))) {
            slug = baseSlug + "-" + suffix++;
        }
        return slug;
    }

    private boolean isValidColumnSlug(String urlSlug) {
        return StringUtils.isNotBlank(urlSlug) && UrlSlugUtil.isValidSlug(urlSlug) && !StringUtils.isNumeric(urlSlug);
    }

    /**
     * 批量发布专栏内所有未发布的文章
     * 
     * @param columnId 专栏ID
     */
    private void batchPublishColumnArticles(Long columnId) {
        // 查询专栏内所有文章ID
        List<ColumnArticleDO> columnArticles = columnArticleDao.lambdaQuery()
                .eq(ColumnArticleDO::getColumnId, columnId)
                .list();
        
        if (CollectionUtils.isEmpty(columnArticles)) {
            return;
        }
        
        List<Long> articleIds = columnArticles.stream()
                .map(ColumnArticleDO::getArticleId)
                .collect(Collectors.toList());
        
        // 查询这些文章中状态为未发布(0)的文章
        List<ArticleDO> offlineArticles = articleDao.lambdaQuery()
                .in(ArticleDO::getId, articleIds)
                .eq(ArticleDO::getStatus, 0)
                .list();
        
        if (CollectionUtils.isEmpty(offlineArticles)) {
            return;
        }
        
        List<Long> offlineArticleIds = offlineArticles.stream()
                .map(ArticleDO::getId)
                .collect(Collectors.toList());
        
        // 批量更新为已发布状态
        articleDao.batchUpdateArticleStatus(offlineArticleIds, 1);
    }

    /**
     * section 的排序规则
     * 1. 以父节点的 section * 1000 为前缀，然后在同一个父节点中，按照顺序找位置（新增，则放在最后，修改则放在对应位置，并将其之后的进行顺移）
     *
     * @param req
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveColumnArticleGroup(ColumnArticleGroupReq req) {
        ColumnArticleGroupDO groupDO = columnStructMapper.toGroupDO(req);
        if (!NumUtil.nullOrZero(req.getId())) {
            // 更新分组
            groupDO.setId(req.getId());
        } else {
            // 表示新增
            groupDO.setId(null);
        }
        if (!firstAddGroup(groupDO)) {
            this.autoCalculateGroupSection(groupDO);
        }
    }

    private boolean firstAddGroup(ColumnArticleGroupDO groupDO) {
        // 如果这个分组是当前专栏的第一个分组；那么就直接新增分组；并将所有未分组的教程全部挂在这个分组下面
        List<ColumnArticleGroupDO> dbRecords = columnArticleGroupDao.selectByColumnId(groupDO.getColumnId());
        if (!CollectionUtils.isEmpty(dbRecords)) {
            return false;
        }

        groupDO.setSection(1L);
        columnArticleGroupDao.save(groupDO);

        // 刷新这个专栏下所有教程的groupId
        columnArticleDao.updateColumnGroupId(groupDO.getId(), groupDO.getColumnId());
        return true;
    }

    /**
     * 这里是兼容老的，直接通过修改分组的sort值来改变分组排序的场景；整个逻辑相对复杂、不容易理解；
     * 可以结合 拖拽排序 moveColumnGroup 的实现进行对照，最终的表现一致，但是实现层和理解层差别还是很大的；一个好的接口设计，可以有效的降低实现复杂度
     *
     * @param currentGroup
     */
    private void autoCalculateGroupSection(ColumnArticleGroupDO currentGroup) {
        long baseSection = 0;
        if (NumUtil.nullOrZero(currentGroup.getParentGroupId())) {
            // 当前节点为顶级节点
            currentGroup.setParentGroupId(0L);
        } else {
            // 找到父节点
            ColumnArticleGroupDO parentGroup = columnArticleGroupDao.getById(currentGroup.getParentGroupId());
            Asserts.notNull(parentGroup, "父节点非法");
            baseSection = parentGroup.getSection() * ColumnArticleGroupDao.SECTION_STEP;
        }

        // 找同一个父节点的子节点
        List<ColumnArticleGroupDO> list = columnArticleGroupDao.selectColumnGroupsBySameParent(currentGroup.getColumnId(), currentGroup.getParentGroupId());
        if (CollectionUtils.isEmpty(list)) {
            // 没有兄弟节点，当前为第一个
            currentGroup.setSection(baseSection + 1L);

            if (!NumUtil.nullOrZero(currentGroup.getId())) {
                // 更新节点，需要同步更新这个节点下面的所有子节点的顺序
                updateGroupSection(currentGroup);
            } else {
                // 新增节点
                columnArticleGroupDao.saveOrUpdate(currentGroup);
            }
            return;
        }


        // 当前为新增节点时，找最大的一个顺序，进行 + 1即可
        if (NumUtil.nullOrZero(currentGroup.getId())) {
            // 新增节点
            currentGroup.setSection(list.get(list.size() - 1).getSection() + 1L);
            columnArticleGroupDao.saveOrUpdate(currentGroup);
            return;
        }

        // 当前节点为更新时，则需要找到当前节点，并更新前/后的节点顺序
        int oldIndex = -1; // 原来在的位置
        int newIndex = 0; // 按照新的排序，应该插入的位置
        for (int i = 0; i < list.size(); i++) {
            ColumnArticleGroupDO item = list.get(i);
            if (item.getId().equals(currentGroup.getId())) {
                oldIndex = i;
                if (item.getSection().equals(currentGroup.getSection())) {
                    // 排序没有改变；只需要更新当前节点的信息即可
                    columnArticleGroupDao.saveOrUpdate(currentGroup);
                    return;
                }
            }

            if (currentGroup.getSection() > item.getSection()) {
                newIndex = i + 1;
            }
        }

        if (oldIndex == newIndex) {
            // 没有改变位置，只需要更新当前节点的子节点顺序
            updateGroupSection(currentGroup);
            return;
        }

        if (oldIndex == -1) {
            // 表示节点为其他的父节点移动过来的， newIndex后面的节点都需要向后移动一位
            long oldSection = currentGroup.getSection() + 1;
            for (int i = newIndex; i < list.size(); i++) {
                ColumnArticleGroupDO item = list.get(i);
                item.setSection(oldSection);
                oldSection += 1;
                updateGroupSection(item);
            }
        } else if (newIndex > oldIndex) {
            // 更新节点，当前节点向后移动了
            long oldSection = list.get(oldIndex).getSection();
            for (int i = oldIndex + 1; i < newIndex; i++) {
                ColumnArticleGroupDO item = list.get(i);
                item.setSection(oldSection);
                oldSection += 1;
                updateGroupSection(item);
            }
        } else {
            // 更新节点，当前节点向前移动了
            long oldSection = currentGroup.getSection() + 1;
            for (int i = newIndex; i < oldIndex; i++) {
                ColumnArticleGroupDO item = list.get(i);
                item.setSection(oldSection);
                oldSection += 1;
                updateGroupSection(item);
            }
        }
    }


    /**
     * 将文章保存到对应的专栏中
     *
     * @param articleId
     * @param columnId
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveColumnArticle(Long articleId, Long columnId) {
        // 转换参数
        // 插入的时候，需要判断是否已经存在
        ColumnArticleDO exist = columnArticleDao.getOne(Wrappers.<ColumnArticleDO>lambdaQuery()
                .eq(ColumnArticleDO::getArticleId, articleId));
        if (exist != null) {
            if (!Objects.equals(columnId, exist.getColumnId())) {
                // 更新
                exist.setColumnId(columnId);
                columnArticleDao.updateById(exist);
            }
        } else {
            // 将文章保存到专栏中，章节序号+1
            ColumnArticleDO columnArticleDO = new ColumnArticleDO();
            columnArticleDO.setColumnId(columnId);
            columnArticleDO.setArticleId(articleId);
            // section 自增+1
            Integer maxSection = columnArticleDao.selectMaxSection(columnId);
            columnArticleDO.setSection(maxSection + 1);
            columnArticleDao.save(columnArticleDO);
        }

        this.autoUpdateColumnArticleSections(columnId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveColumnArticle(ColumnArticleReq req) {
        // 转换参数
        ColumnArticleDO columnArticleDO = ColumnArticleStructMapper.INSTANCE.reqToDO(req);
        columnArticleDO.setSection(req.getSort());
        columnArticleDO.setReadType(req.getRead());
        columnArticleDO.setPreviewPercent(normalizePreviewPercent(req.getPreviewPercent()));
        if (NumUtil.nullOrZero(columnArticleDO.getId())) {
            // 插入的时候，需要判断是否已经存在
            ColumnArticleDO exist = columnArticleDao.getOne(Wrappers.<ColumnArticleDO>lambdaQuery()
                    .eq(ColumnArticleDO::getColumnId, columnArticleDO.getColumnId())
                    .eq(ColumnArticleDO::getArticleId, columnArticleDO.getArticleId()));
            if (exist != null) {
                throw ExceptionUtil.of(StatusEnum.COLUMN_ARTICLE_EXISTS, "请勿重复添加");
            }

            // section 自增+1
            Integer maxSection = columnArticleDao.selectMaxSection(columnArticleDO.getColumnId());
            columnArticleDO.setSection(maxSection + 1);
            columnArticleDao.save(columnArticleDO);
        } else {
            columnArticleDao.updateById(columnArticleDO);
        }

        // 同时，更新 article 的 shortTitle 短标题
        if (req.getShortTitle() != null) {
            ArticleDO articleDO = new ArticleDO();
            articleDO.setShortTitle(req.getShortTitle());
            articleDO.setId(req.getArticleId());
            articleDao.updateById(articleDO);
        }

        this.autoUpdateColumnArticleSections(columnArticleDO.getColumnId());
    }

    private Integer normalizePreviewPercent(Integer previewPercent) {
        if (previewPercent == null || previewPercent <= 0) {
            return 0;
        }
        if (previewPercent > 100) {
            return 100;
        }
        return previewPercent;
    }

    @Override
    public void deleteColumn(Long columnId) {
        columnDao.deleteColumn(columnId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteColumnArticle(Long id) {
        ColumnArticleDO columnArticleDO = columnArticleDao.getById(id);
        if (columnArticleDO != null) {
            columnArticleDao.removeById(id);
            // 删除的时候，批量更新 section，比如说原来是 1,2,3,4,5,6,7,8,9,10，删除 5，那么 6-10 的 section 都要减 1
            columnArticleDao.update(null, Wrappers.<ColumnArticleDO>lambdaUpdate().setSql("section = section - 1")
                    .eq(ColumnArticleDO::getColumnId, columnArticleDO.getColumnId())
                    // section 大于 1
                    .gt(ColumnArticleDO::getSection, 1).gt(ColumnArticleDO::getSection, columnArticleDO.getSection()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortColumnArticleApi(SortColumnArticleReq req) {
        // 根据 req 的两个 ID 调换两篇文章的顺序
        ColumnArticleDO activeDO = columnArticleDao.getById(req.getActiveId());
        ColumnArticleDO overDO = columnArticleDao.getById(req.getOverId());
        if (activeDO != null && overDO != null && !activeDO.getId().equals(overDO.getId())) {
            Integer activeSection = activeDO.getSection();
            Integer overSection = overDO.getSection();
            // 假如原始顺序为1、2、3、4
            //
            //把 1 拖到 4 后面 2 变 1 3 变 2 4 变 3 1 变 4
            //把 1 拖到 3 后面 2 变 1 3 变 2 4 不变 1 变 3
            //把 1 拖到 2 后面 2 变 1 3 不变 4 不变 1 变 2
            //把 2 拖到 4 后面 1 不变 3 变 2 4 变 3 2 变 4
            //把 2 拖到 3 后面 1 不变 3 变 2 4 不变 2 变 3
            //把 3 拖到 4 后面 1 不变 2 不变 4 变 3 3 变 4
            //把 4 拖到 1 前面 1 变 2 2 变 3 3 变 4
            //把 4 拖到 2 前面 1 不变 2 变 3 3 变 4  4 变 1
            //把 4 拖到 3 前面 1 不变 2 不变 3 变 4 4 变 1
            //把 3 拖到 1 前面 1 变 2 2 变 3 3 变 4 4 变 1
            //依次类推
            // 1. 如果 activeSection > overSection，那么 activeSection - 1 到 overSection 的 section 都要 +1
            // 向上拖动
            if (activeSection > overSection) {
                // 当 activeSection 大于 overSection 时，表示文章被向上拖拽。
                // 需要将 activeSection 到 overSection（不包括 activeSection 本身）之间的所有文章的 section 加 1，
                // 并将 activeSection 设置为 overSection。
                columnArticleDao.update(null, Wrappers.<ColumnArticleDO>lambdaUpdate()
                        .setSql("section = section + 1") // 将符合条件的记录的 section 字段的值增加 1
                        .eq(ColumnArticleDO::getColumnId, overDO.getColumnId()) // 指定要更新记录的 columnId 条件
                        .ge(ColumnArticleDO::getSection, overSection) // 指定 section 字段的下限（包含此值）
                        .lt(ColumnArticleDO::getSection, activeSection)); // 指定 section 字段的上限

                // 将 activeDO 的 section 设置为 overSection
                activeDO.setSection(overSection);
                columnArticleDao.updateById(activeDO);
            } else {
                // 2. 如果 activeSection < overSection，
                // 那么 activeSection + 1 到 overSection 的 section 都要 -1
                // 向下拖动
                // 需要将 activeSection 到 overSection（包括 overSection）之间的所有文章的 section 减 1
                columnArticleDao.update(null, Wrappers.<ColumnArticleDO>lambdaUpdate()
                        .setSql("section = section - 1") // 将符合条件的记录的 section 字段的值减少 1
                        .eq(ColumnArticleDO::getColumnId, overDO.getColumnId()) // 指定要更新记录的 columnId 条件
                        .gt(ColumnArticleDO::getSection, activeSection) // 指定 section 字段的下限（不包含此值）
                        .le(ColumnArticleDO::getSection, overSection)); // 指定 section 字段的上限（包含此值）

                // 将 activeDO 的 section 设置为 overSection -1
                activeDO.setSection(overSection);
                columnArticleDao.updateById(activeDO);

            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortColumnArticleByIDApi(SortColumnArticleByIDReq req) {
        // 获取要重新排序的专栏文章
        ColumnArticleDO columnArticleDO = columnArticleDao.getById(req.getId());
        // 不等于空
        if (columnArticleDO == null) {
            throw ExceptionUtil.of(StatusEnum.COLUMN_ARTICLE_EXISTS, "教程不存在");
        }
        // 如果顺序没变
        if (req.getSort().equals(columnArticleDO.getSection())) {
            return;
        }
        // 获取教程可以调整的最大顺序
        Integer maxSection = columnArticleDao.selectMaxSection(columnArticleDO.getColumnId());
        // 如果输入的顺序大于最大顺序，提示错误
        if (req.getSort() > maxSection) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "顺序超出范围");
        }
        // 查看输入的顺序是否存在
        ColumnArticleDO changeColumnArticleDO = columnArticleDao.selectBySection(columnArticleDO.getColumnId(), req.getSort());
        // 如果存在，交换顺序
        if (changeColumnArticleDO != null) {
            // 交换顺序
            columnArticleDao.update(null, Wrappers.<ColumnArticleDO>lambdaUpdate()
                    .set(ColumnArticleDO::getSection, columnArticleDO.getSection())
                    .eq(ColumnArticleDO::getId, changeColumnArticleDO.getId()));
            columnArticleDao.update(null, Wrappers.<ColumnArticleDO>lambdaUpdate()
                    .set(ColumnArticleDO::getSection, changeColumnArticleDO.getSection())
                    .eq(ColumnArticleDO::getId, columnArticleDO.getId()));
        } else {
            // 如果不存在，直接修改顺序
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "输入的顺序不存在，无法完成交换");
        }
    }

    @Override
    public PageVo<ColumnDTO> getColumnList(SearchColumnReq req) {
        // 转换参数
        ColumnStructMapper mapper = ColumnStructMapper.INSTANCE;
        SearchColumnParams params = mapper.reqToSearchParams(req);
        // 查询
        List<ColumnInfoDO> columnList = columnDao.listColumnsByParams(params, PageParam.newPageInstance(req.getPageNumber(), req.getPageSize()));
        // 转属性
        List<ColumnDTO> columnDTOS = mapper.infoToDtos(columnList);

        // 进行优化，由原来的多次查询用户信息，改为一次查询用户信息
        // 获取所有需要的用户id
        // 判断 columnDTOS 是否为空
        if (CollUtil.isNotEmpty(columnDTOS)) {
            List<Long> userIds = columnDTOS.stream().map(ColumnDTO::getAuthor).collect(Collectors.toList());

            // 查询所有的用户信息
            List<BaseUserInfoDTO> users = userService.batchQueryBasicUserInfo(userIds);

            // 创建一个id到用户信息的映射
            Map<Long, BaseUserInfoDTO> userMap = users.stream()
                    .collect(Collectors.toMap(BaseUserInfoDTO::getId, Function.identity()));

            // 设置作者信息
            columnDTOS.forEach(columnDTO -> {
                BaseUserInfoDTO user = userMap.get(columnDTO.getAuthor());
                if (user != null) {
                    columnDTO.setAuthorName(user.getUserName());
                    columnDTO.setAuthorAvatar(user.getPhoto());
                    columnDTO.setAuthorProfile(user.getProfile());
                }
            });
        }

        Integer totalCount = columnDao.countColumnsByParams(params);
        return PageVo.build(columnDTOS, req.getPageSize(), req.getPageNumber(), totalCount);
    }

    @Override
    public PageVo<ColumnArticleDTO> getColumnArticleList(SearchColumnArticleReq req) {
        // 转换参数
        ColumnArticleStructMapper mapper = ColumnArticleStructMapper.INSTANCE;
        SearchColumnArticleParams params = mapper.toSearchParams(req);
        // 查询
        List<ColumnArticleDTO> simpleArticleDTOS = columnDao.listColumnArticlesDetail(params, PageParam.newPageInstance(req.getPageNumber(), req.getPageSize()));
        int totalCount = columnDao.countColumnArticles(params);
        return PageVo.build(simpleArticleDTOS, req.getPageSize(), req.getPageNumber(), totalCount);
    }

    @Override
    public List<SimpleColumnDTO> listSimpleColumnBySearchKey(String key) {
        LambdaQueryWrapper<ColumnInfoDO> query = Wrappers.lambdaQuery();
        query.select(ColumnInfoDO::getId, ColumnInfoDO::getColumnName, ColumnInfoDO::getCover)
                .and(!StringUtils.isEmpty(key), v -> v.like(ColumnInfoDO::getColumnName, key))
                .orderByDesc(ColumnInfoDO::getId);
        List<ColumnInfoDO> articleDOS = columnDao.list(query);
        return ColumnStructMapper.INSTANCE.infoToSimpleDtos(articleDOS);
    }

    @Override
    public List<ColumnArticleGroupDTO> getColumnGroups(Long columnId) {
        List<ColumnArticleGroupDO> entityList = columnArticleGroupDao.selectByColumnId(columnId);
        if (CollectionUtils.isEmpty(entityList)) {
            return Collections.emptyList();
        }
        List<ColumnArticleGroupDTO> dtoList = ColumnStructMapper.INSTANCE.toGroupDTOList(entityList);
        return TreeBuilder.buildTree(dtoList);
    }


    public List<ColumnArticleGroupDTO> getColumnGroupAndArticles(Long columnId) {
        List<ColumnArticleGroupDO> entityList = columnArticleGroupDao.selectByColumnId(columnId);
        List<ColumnArticleGroupDTO> dtoList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(entityList)) {
            dtoList = ColumnStructMapper.INSTANCE.toGroupDTOList(entityList);
        }
        // 分组，并设置一个未分组的默认分组项，用于挂文章没有指定分组的场景
        Map<Long, ColumnArticleGroupDTO> groupMap = dtoList.stream()
                .collect(Collectors.toMap(ColumnArticleGroupDTO::getGroupId, v -> v));
        ColumnArticleGroupDTO defaultGroup = ColumnArticleGroupDTO.newDefaultGroup(columnId);


        // 查询所有文章
        SearchColumnArticleParams params = new SearchColumnArticleParams();
        params.setColumnId(columnId);
        List<ColumnArticleDTO> articleList = columnDao.listColumnArticlesDetail(params, PageParam.newPageInstance(1, Integer.MAX_VALUE));
        // 将文章放入分组中
        for (ColumnArticleDTO article : articleList) {
            ColumnArticleGroupDTO group = groupMap.getOrDefault(article.getGroupId(), defaultGroup);
            if (group.getArticles() == null) {
                group.setArticles(new ArrayList<>());
            }
            group.getArticles().add(article);
        }
        if (CollectionUtils.isNotEmpty(defaultGroup.getArticles())) {
            dtoList.add(0, defaultGroup);
        }
        return TreeBuilder.buildTree(dtoList);
    }


    /**
     * 删除专栏内的文章分组
     *
     * @param groupId 分组id
     * @return true 表示删除成功， false 表示删除失败
     */
    @Override
    public boolean deleteColumnGroup(Long groupId) {
        ColumnArticleGroupDO group = columnArticleGroupDao.getById(groupId);
        if (group == null) {
            throw ExceptionUtil.of(StatusEnum.RECORDS_NOT_EXISTS, groupId);
        }

        // 判断这个分组下是否有子分组，如果有，则不允许直接删除
        ColumnArticleGroupDO sub = columnArticleGroupDao.selectByParentGroupId(groupId);
        if (sub != null) {
            throw ExceptionUtil.of(StatusEnum.UNEXPECT_ERROR, "存在子分组，不支持直接删除当前分组!");
        }

        // 判断分组下是否存在文章，如果存在，也不允许删除
        ColumnArticleDO article = columnArticleDao.selectOneByGroupId(groupId);
        if (article != null) {
            throw ExceptionUtil.of(StatusEnum.UNEXPECT_ERROR, "分组下已经有文章了，不支持直接删除当前分组!");
        }

        // 直接删除
        group.setDeleted(YesOrNoEnum.YES.getCode());
        group.setUpdateTime(new Date());
        return columnArticleGroupDao.updateById(group);
    }

    @Override
    @Transactional
    public boolean moveColumnArticleOrGroup(MoveColumnArticleOrGroupReq req) {
        if (NumUtil.upZero(req.getMoveArticleId())) {
            // 移动教程
            return moveColumnArticle(req);
        } else {
            // 移动分组
            return moveColumnGroup(req);
        }
    }

    private boolean moveColumnArticle(MoveColumnArticleOrGroupReq req) {
        if (NumUtil.upZero(req.getTargetGroupId())) {
            // 移动到目标分组的前后，则表示将当前教程移动到目标分组的父分组下，作为第一篇教程
            // 将当前教程的排序设置为1，父分组下其他教程的排序 + 1
            ColumnArticleGroupDO targetGroup = columnArticleGroupDao.getById(req.getTargetGroupId());
            if (targetGroup == null) {
                throw ExceptionUtil.of(StatusEnum.RECORDS_NOT_EXISTS, req.getTargetGroupId());
            }

            if (Objects.equals(req.getMovePosition(), MovePositionEnum.IN.getCode())) {
                // 移动到目标分组里面，将这个分组中所有的教程排序 + 1
                columnArticleDao.updateColumnArticleGESectionToAdd(req.getColumnId(), targetGroup.getId(), 1, 1);
                // 将当前教程设置为第一篇
                columnArticleDao.updateColumnArticleSection(req.getColumnId(), req.getMoveArticleId(), targetGroup.getId(), 1);
            } else {
                // 移动到目标分组的父分组下，则表示将当前教程移动到目标分组的父分组下，作为第一篇教程
                columnArticleDao.updateColumnArticleGESectionToAdd(req.getColumnId(), targetGroup.getParentGroupId(), 1, 1);
                columnArticleDao.updateColumnArticleSection(req.getColumnId(), req.getMoveArticleId(), targetGroup.getParentGroupId(), 1);
            }
        } else {
            // 移动到目标教程的前后，则需要更新groupId，
            // 如果是后，将这个分组中，教程后面的所有教程的排序 + 2，当前教程的排序 = 目标教程的排序 + 1
            // 如果是前，将这个分组中，目标教程及之后的所有教程排序 + 1，当前教程的排序 = 目标教程的排序
            ColumnArticleDO targetArticle = columnArticleDao.selectColumnArticle(req.getColumnId(), req.getTargetArticleId());
            if (targetArticle == null) {
                throw ExceptionUtil.of(StatusEnum.RECORDS_NOT_EXISTS, req.getTargetArticleId());
            }

            if (Objects.equals(req.getMovePosition(), MovePositionEnum.BEFORE.getCode())) {
                // 移动到目标教程前，表示替换目标教程的顺序
                columnArticleDao.updateColumnArticleGESectionToAdd(req.getColumnId(), targetArticle.getGroupId(), targetArticle.getSection(), 1);
                columnArticleDao.updateColumnArticleSection(req.getColumnId(), req.getMoveArticleId(), targetArticle.getGroupId(), targetArticle.getSection());
            } else {
                // 移动到目标教程后
                columnArticleDao.updateColumnArticleGESectionToAdd(req.getColumnId(), targetArticle.getGroupId(), targetArticle.getSection() + 1, 2);
                columnArticleDao.updateColumnArticleSection(req.getColumnId(), req.getMoveArticleId(), targetArticle.getGroupId(), targetArticle.getSection() + 1);
            }
        }

        // 专栏内教程顺序重排
        this.autoUpdateColumnArticleSections(req.getColumnId());
        return true;
    }

    /**
     * 前台在显示文章时，是按照顺序递增的方式进行访问的，因此拖拽教程之后，我们对专栏内的教程做一次重排
     *
     * @param columnId
     */
    private void autoUpdateColumnArticleSections(Long columnId) {
        SearchColumnArticleParams params = new SearchColumnArticleParams();
        params.setColumnId(columnId);
        List<ColumnArticleDTO> articleList = columnDao.listColumnArticlesDetail(params, PageParam.newPageInstance(1, Integer.MAX_VALUE));
        int section = 1;
        for (ColumnArticleDTO item : articleList) {
            columnArticleDao.updateColumnArticleSection(item.getId(), section);
            section += 1;
        }
    }

    /**
     * 将一个教程分组，移动到另一个教程分组前、后、里
     *
     * @param req
     * @return
     */
    private boolean moveColumnGroup(MoveColumnArticleOrGroupReq req) {
        ColumnArticleGroupDO currentGroup = columnArticleGroupDao.getById(req.getMoveGroupId());
        ColumnArticleGroupDO targetGroup = columnArticleGroupDao.getById(req.getTargetGroupId());

        if (Objects.equals(req.getMovePosition(), MovePositionEnum.IN.getCode())) {
            // 移动到目标分组内，作为该分组的第一个；目标分组中的其他分组全部往后移动一位
            List<ColumnArticleGroupDO> subGroups = columnArticleGroupDao.selectColumnGroupsBySameParent(req.getColumnId(), targetGroup.getId());
            AtomicLong baseSection = new AtomicLong(targetGroup.getSection() * ColumnArticleGroupDao.SECTION_STEP + 1);
            currentGroup.setParentGroupId(targetGroup.getId());
            currentGroup.setSection(baseSection.get());
            this.updateGroupSection(currentGroup);
            subGroups.forEach(sub -> {
                if (!sub.getId().equals(currentGroup.getId())) {
                    baseSection.addAndGet(1);
                    sub.setSection(baseSection.get());
                    updateGroupSection(sub);
                }
            });
        } else if (Objects.equals(req.getMovePosition(), MovePositionEnum.BEFORE.getCode())) {
            // 移动到目标分组前，则将目标分组的排序 + 1，当前分组的教程排序 = 目标分组的排序
            List<ColumnArticleGroupDO> subGroups = columnArticleGroupDao.selectColumnGroupsBySameParent(req.getColumnId(), targetGroup.getParentGroupId());

            // 首先将当前分组，从列表中移除
            subGroups.removeIf(item -> item.getId().equals(currentGroup.getId()));

            // 找到目标分组，在列表中的位置
            long baseSection = -1;
            for (int i = 0; i < subGroups.size(); i++) {
                ColumnArticleGroupDO item = subGroups.get(i);
                if (item.getId().equals(targetGroup.getId())) {
                    baseSection = item.getSection();
                    currentGroup.setParentGroupId(targetGroup.getParentGroupId());
                    currentGroup.setSection(baseSection);
                    this.updateGroupSection(currentGroup);

                    baseSection += 1;
                    item.setSection(baseSection);
                    this.updateGroupSection(item);
                    baseSection += 1;
                    continue;
                }
                if (baseSection > 0) {
                    // 表示已经找到了目标分组
                    item.setSection(baseSection);
                    baseSection += 1;
                    this.updateGroupSection(item);
                }
            }
        } else {
            // 移动到目标分组后，则将目标分组后面的分组排序 + 1，当前分组的教程排序 = 目标分组的排序 + 1
            List<ColumnArticleGroupDO> subGroups = columnArticleGroupDao.selectColumnGroupsBySameParent(req.getColumnId(), targetGroup.getParentGroupId());

            // 首先将当前分组，从列表中移除
            subGroups.removeIf(item -> item.getId().equals(currentGroup.getId()));

            // 找到目标分组，在列表中的位置
            long baseSection = -1;
            for (int i = 0; i < subGroups.size(); i++) {
                ColumnArticleGroupDO item = subGroups.get(i);
                if (item.getId().equals(targetGroup.getId())) {
                    baseSection = item.getSection() + 1;
                    currentGroup.setParentGroupId(targetGroup.getParentGroupId());
                    currentGroup.setSection(baseSection);
                    this.updateGroupSection(currentGroup);

                    baseSection += 1;
                    continue;
                }
                if (baseSection > 0) {
                    // 表示已经找到了目标分组
                    item.setSection(baseSection);
                    baseSection += 1;
                    this.updateGroupSection(item);
                }
            }
        }
        // 专栏内教程顺序重排
        this.autoUpdateColumnArticleSections(req.getColumnId());
        return true;
    }

    /**
     * 更新当前节点，和对应子节点的顺序
     *
     * @param groupDO
     */
    private void updateGroupSection(ColumnArticleGroupDO groupDO) {
        // 更新当前节点的顺序
        columnArticleGroupDao.updateById(groupDO);
        // 更新子节点的顺序
        List<ColumnArticleGroupDO> children = columnArticleGroupDao.selectColumnGroupsBySameParent(groupDO.getColumnId(), groupDO.getId());
        long baseSection = groupDO.getSection() * ColumnArticleGroupDao.SECTION_STEP;
        for (int i = 0; i < children.size(); i++) {
            ColumnArticleGroupDO item = children.get(i);
            item.setSection(baseSection + i + 1);
            updateGroupSection(item);
        }
    }
}
