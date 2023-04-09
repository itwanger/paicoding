package com.github.paicoding.forum.test.javabetter.socket1.httpserver;

import cn.hutool.json.JSONUtil;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Created by @author yihui in 14:45 18/12/13.
 */
public class ReflectUtil {

    /**
     * 遍历类信息，获取成员属性；静态类的调用时，bean应该为null； 实例调用时，为实例本身
     *
     * @param bean
     * @param clz
     * @param fieldName
     * @return 返回field对象及其类型，因为field对象可以为null，所以这里直接获取反射的class类型返回；
     * @throws IllegalAccessException
     */
    public static ImmutablePair<Object, Class> getField(Object bean, Class clz, String fieldName)
            throws IllegalAccessException {
        if (clz == Object.class) {
            throw new ServerNotFoundException(
                    "can't find field by fieldName: " + fieldName + " for clz:" + clz.getName());
        }

        for (Field field : clz.getDeclaredFields()) {
            if (field.getName().equals(fieldName)) {
                field.setAccessible(true);
                return ImmutablePair.of(field.get(bean), field.getType());
            }
        }

        return getField(bean, clz.getSuperclass(), fieldName);
    }


    /**
     * 从当前类和父类中查找对应的方法
     *
     * @param clz
     * @param method
     * @param args
     * @return
     */
    public static Method getMethod(Class clz, String method, ImmutablePair<Type, Object>[] args) {
        if (clz == Object.class) {
            throw new ServerNotFoundException(
                    "can't find method by methodName: " + method + " args: " + JSONUtil.toJsonStr(args) + " for clz:" +
                            clz.getName());
        }


        for (Method m : clz.getDeclaredMethods()) {
            if (!m.getName().equals(method) || m.getParameterCount() != args.length) {
                continue;
            }

            if (judgeParamsType(m.getParameterTypes(), args)) {
                m.setAccessible(true);
                return m;
            }
        }

        return getMethod(clz.getSuperclass(), method, args);
    }

    private static boolean judgeParamsType(Class[] paramTypes, ImmutablePair<Type, Object>[] args) {
        for (int index = 0; index < args.length; index++) {
            if (!judgeTypeMatch(paramTypes[index], args[index].getRight() != null ? args[index].getRight().getClass() :
                    (Class) args[index].getLeft())) {
                // 判断定义的参数类型，是否为传参类型，或者传参的父类or接口类型，不满足时，直接判False
                return false;
            }
        }

        return true;
    }

    /**
     * 判断类型是否兼容
     *
     * @param base
     * @param target
     * @return
     */
    private static boolean judgeTypeMatch(Class base, Class target) {
        if (base.isAssignableFrom(target)) {
            // 类型相同，或者base为target的父类、接口类型
            return true;
        }

        if (base == int.class) {
            return target == Integer.class;
        } else if (base == Integer.class) {
            return target == int.class;
        } else if (base == long.class) {
            return target == Long.class;
        } else if (base == Long.class) {
            return target == long.class;
        } else if (base == float.class) {
            return target == Float.class;
        } else if (base == Float.class) {
            return target == float.class;
        } else if (base == double.class) {
            return target == Double.class;
        } else if (base == Double.class) {
            return target == double.class;
        } else if (base == boolean.class) {
            return target == Boolean.class;
        } else if (base == Boolean.class) {
            return target == boolean.class;
        } else if (base == char.class) {
            return target == Character.class;
        } else if (base == Character.class) {
            return target == char.class;
        } else if (base == byte.class) {
            return target == Byte.class;
        } else if (base == Byte.class) {
            return target == byte.class;
        } else if (base == short.class) {
            return target == Short.class;
        } else if (base == Short.class) {
            return target == short.class;
        } else {
            return false;
        }
    }

    private static Object execute(Object bean, Class clz, String method, ImmutablePair<Type, Object>[] args) {
        if (StringUtils.isEmpty(method)) {
            // 获取类的成员属性值时，不传method，直接返回属性值
            return bean;
        }

        Method chooseMethod = getMethod(clz, method, args);

        if (chooseMethod == null) {
            throw new ServerNotFoundException("can't find server's method: " + clz.getName() + "#" + method);
        }

        try {
            chooseMethod.setAccessible(true);
            Object[] params = new Object[args.length];
            for (int index = 0, size = args.length; index < size; index++) {
                params[index] = args[index].getRight();
            }
            return chooseMethod.invoke(bean, params);
        } catch (Exception e) {
            throw new ServerInvokedException(
                    "unexpected server invoked " + clz.getName() + "#" + method + " args: " + JSONUtil.toJsonStr(args),
                    e);
        }
    }

    public static Object execute(Object target, Class clz, ReflectReqDTO req) {
        ImmutablePair<Type, Object>[] args = ArgumentParser.parse(req.getParams());
        target = execute(target, clz, req.getMethod(), args);

        if (target == null) {
            return null;
        }

        // 如果存在二级调用时，继续走下面的逻辑
        try {
            if (!StringUtils.isBlank(req.getSecondField())) {
                target = ReflectUtil.getField(target, target.getClass(), req.getSecondField()).getLeft();
            }
        } catch (Exception e) {
            throw new ServerNotFoundException("get " + target.getClass() + "#" + req.getSecondField() + " error!", e);
        }

        if (!StringUtils.isBlank(req.getSecondMethod())) {
            args = ArgumentParser.parse(req.getSecondParams());
            target = execute(target, target.getClass(), req.getSecondMethod(), args);
        }
        return target;
    }
}