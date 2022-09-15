package com.github.liuyueyi.forum.service.article.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.liueyueyi.forum.api.model.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author YiHui
 * @date 2022/9/14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("column_info")
public class ColumnInfoDO extends BaseDO {

    private static final long serialVersionUID = 1920830534262012026L;

    private String name;

    private String summary;

    private String cover;

    private Integer state;
}
