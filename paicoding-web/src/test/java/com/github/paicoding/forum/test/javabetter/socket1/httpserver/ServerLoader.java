package com.github.paicoding.forum.test.javabetter.socket1.httpserver;

/**
 * 用来实现加载应用中的实例或静态类的接口
 *
 * Created by @author yihui in 14:57 18/12/29.
 */
@LoaderOrder
public interface ServerLoader {
    /**
     * 返回优先级
     *
     * @return
     */
    default int order() {
        try {
            return this.getClass().getAnnotation(LoaderOrder.class).order();
        } catch (Exception e) {
            return 10;
        }
    }

    /**
     * ServerLoader是否支持获取目标对象
     *
     * @param reqDTO
     * @return
     */
    boolean enable(ReflectReqDTO reqDTO);

    /**
     * 根据传入参数，获取目标对象和目标对象的class
     *
     * @param reqDTO
     * @return
     */
    ImmutablePair</** 目标对象 */Object, /*** 目标对象类型 */Class> getInvokeObject(ReflectReqDTO reqDTO);


    Object getInvokeObject(String key);

}