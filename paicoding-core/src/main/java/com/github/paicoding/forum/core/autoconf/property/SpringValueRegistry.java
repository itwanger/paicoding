package com.github.paicoding.forum.core.autoconf.property;

import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.core.util.StrUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * 配置变更注册
 *
 * @author YiHui
 * @date 2023/6/26
 */
@Slf4j
public class SpringValueRegistry {
    @Data
    public static class SpringValue {
        /**
         * 适合用于：配置是通过set类方法实现注入绑定的方式，只有一个传参，为对应的配置key
         */
        private MethodParameter methodParameter;
        /**
         * 成员变量
         */
        private Field field;
        /**
         * bean示例的弱引用
         */
        private WeakReference<Object> beanRef;
        /**
         * Spring Bean Name
         */
        private String beanName;
        /**
         * 配置对应的key： 如 config.user
         */
        private String key;
        /**
         * 配置引用，如 ${config.user}
         */
        private String placeholder;
        /**
         * 配置绑定的目标类型
         */
        private Class<?> targetType;

        public SpringValue(String key, String placeholder, Object bean, String beanName, Field field) {
            this.beanRef = new WeakReference<>(bean);
            this.beanName = beanName;
            this.field = field;
            this.placeholder = placeholder;
            this.targetType = field.getType();
            this.formatKey(key);
        }

        public SpringValue(String key, String placeholder, Object bean, String beanName, Method method) {
            this.beanRef = new WeakReference<>(bean);
            this.beanName = beanName;
            this.methodParameter = new MethodParameter(method, 0);
            this.placeholder = placeholder;
            Class<?>[] paramTps = method.getParameterTypes();
            this.targetType = paramTps[0];
            this.formatKey(key);
        }

        private void formatKey(String key) {
            this.key = StrUtil.formatSpringConfigKey(key);
            if (!Objects.equals(key, this.key)) {
                log.info("配置key格式化输出: {} -> {}", key, this.key);
            }
        }

        /**
         * 配置基于反射的动态变更
         *
         * @param newVal String: 配置对应的key   Class: 配置绑定的成员/方法参数类型， Object 新的配置值
         * @throws Exception
         */
        public void update(BiFunction<String, Class, Object> newVal) throws Exception {
            if (isField()) {
                injectField(newVal);
            } else {
                injectMethod(newVal);
            }
        }

        private void injectField(BiFunction<String, Class, Object> newVal) throws Exception {
            Object bean = beanRef.get();
            if (bean == null) {
                return;
            }
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            field.set(bean, newVal.apply(key, field.getType()));
            field.setAccessible(accessible);
            if (log.isDebugEnabled()) {
                log.debug("更新value: {}#{} = {}", beanName, field.getName(), field.get(bean));
            }
        }

        private void injectMethod(BiFunction<String, Class, Object> newVal)
                throws Exception {
            Object bean = beanRef.get();
            if (bean == null) {
                return;
            }
            Object va = newVal.apply(key, methodParameter.getParameterType());
            methodParameter.getMethod().invoke(bean, va);
            log.info("更新method: {}#{} = {}", beanName, methodParameter.getMethod().getName(), va);
        }

        public boolean isField() {
            return this.field != null;
        }
    }


    public static Map<String, Set<SpringValue>> registry = new ConcurrentHashMap<>();

    /**
     * 像registry中注册配置key绑定的对象W
     *
     * @param key
     * @param val
     */
    public static void register(String key, SpringValue val) {
        if (!registry.containsKey(key)) {
            synchronized (SpringValueRegistry.class) {
                if (!registry.containsKey(key)) {
                    registry.put(key, new HashSet<>());
                }
            }
        }

        Set<SpringValue> set = registry.getOrDefault(key, new HashSet<>());
        set.add(val);
    }

    /**
     * key对应的配置发生了变更，找到绑定这个配置的属性，进行反射刷新
     *
     * @param key
     */
    public static void updateValue(String key) {
        // 项目启动时，有一个配置，没有再配置文件中初始化，而是直接再应用代码中写上了默认值，此时若直接走下面的更新流程，会导致配置绑定异常，项目启动失败
        // 因此我们再执行更新时，先判断下配置上下文中是否有这个配置
        // fixme: 那么问题来了，如果是删除了一个动态配置，那应该怎么将应用中的配置刷新为默认值呢？
        if (!SpringUtil.hasConfig(key)) {
            return;
        }

        Set<SpringValue> set = registry.getOrDefault(key, new HashSet<>());
        set.forEach(s -> {
            try {
                s.update((s1, aClass) -> SpringUtil.getBinder().bindOrCreate(s1, aClass));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
