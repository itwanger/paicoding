package com.github.paicoding.forum.test.javabetter.socket1.httpserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by @author yihui in 14:58 18/12/29.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReflectReqDTO implements Serializable {
    private static final long serialVersionUID = -151408688916877734L;
    /**
     * 调用的服务名，.class 结尾，则表示根据类型查找Spring容器中的Bean；否则表示传入的为beanName，通过name方式查找Spring容器中的Bean
     */
    private String service;

    /**
     * type用来区分service传入的是bean还是静态类
     *
     * 当type == static 时要求service传入对应的静态类完整包路径方式
     */
    private String type;

    /**
     * 需要执行的方法
     */
    private String method;

    /**
     * 非空：表示最终执行的是service这个bean中成员field的方法method
     * 空：  表示最终执行的是service这个bean提供的方法method
     */
    private String field;

    /**
     * 请求参数，格式为  class#value, 如
     *
     * - int#20
     * - Integer#20
     * - String#Hello World
     * - net.finbtc.component.model.TradePairDO#{"pairId": 120}
     */
    private String[] params;

    /**
     * 针对单例的访问/针对静态类的方法返回对象再次访问的case
     */
    private String secondMethod;

    private String secondField;

    private String[] secondParams;
}
