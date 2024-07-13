package com.github.paicoding.forum.core.config;

import lombok.Data;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
public class OssProperties {
    /**
     * 上传文件前缀路径
     */
    private String prefix;
    /**
     * oss类型
     */
    private String type;
    /**
     * 下面几个是oss的配置参数
     */
    private String endpoint;
    private String ak;
    private String sk;
    private String bucket;
    private String host;
}
