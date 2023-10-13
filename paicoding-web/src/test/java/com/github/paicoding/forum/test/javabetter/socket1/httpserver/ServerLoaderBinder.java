package com.github.paicoding.forum.test.javabetter.socket1.httpserver;

import java.util.List;

@LoaderOrder
public interface ServerLoaderBinder {
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
     * 获取框架所有支持的ServerLoader，用于获取不同场景下获取应用中的内存实例
     *
     * @return
     */
    List<ServerLoader> getBeanLoader();

}