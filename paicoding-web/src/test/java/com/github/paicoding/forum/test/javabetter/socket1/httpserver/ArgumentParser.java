package com.github.paicoding.forum.test.javabetter.socket1.httpserver;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * 根据传入的参数来解析为对应的do对象
 * Created by @author yihui in 15:32 18/12/13.
 */
public class ArgumentParser {
    public static final String SPLIT_SYMBOL = "#";

    /**
     * default empty arguments
     */
    private static final ImmutablePair[] EMPTY_ARGS = new ImmutablePair[0];

    private static List<IArgParser> parseList;

    @SuppressWarnings("unchecked")
    public static ImmutablePair<Type, Object>[] parse(String[] args) {
        if (args == null || args.length == 0) {
            return EMPTY_ARGS;
        }

        ImmutablePair[] result = new ImmutablePair[args.length];
        for (int i = 0; i < args.length; i++) {
            result[i] = buildArgObj(args[i]);
        }
        return result;
    }

    /**
     * 将传入的String类型参数封装为目标对象
     *
     * @param arg 以#分割，根据我们的定义，
     *            第一个#前为目标对象类型，
     *            最后一个#后为目标对象值（如果为JOPO，则采用json方式进行反序列化）
     *            中间的作为泛型的参数类型传入
     *
     *            几个常见的case如:
     *
     *            "Hello World"  返回 "Hello Word"
     *            "int#10" 返回 10
     *            "enum#com.git.hui.fix.example.springmvc.rest.EnumBean.DemoType#GET" 枚举，相当于传参 DemoType.GET
     *            "DefaultServerBinder#{}" 返回的是对象 defaultServerBinder
     *            "java.util.List<java.lang.String, java.lang.String>>#["ads","bcd"]  返回的是List集合, 相当于  Arrays.asList("asd", "bcd")
     * @return
     */
    private static ImmutablePair<Type, Object> buildArgObj(String arg) {
        String[] typeValue = StringUtils.split(arg, SPLIT_SYMBOL);
        if (typeValue.length == 1) {
            // 没有 #，把参数当成String
            return parseStrToObj("", arg);
        } else if (typeValue.length == 2) {
            // 标准的kv参数, 前面为参数类型，后面为参数值
            return parseStrToObj(typeValue[0], typeValue[1]);
        } else {
            return parseStrToObj(typeValue[0], arg.substring(typeValue[0].length() + 1));
        }
    }

    private static ImmutablePair<Type, Object> parseStrToObj(String type, String value) {
        loadParser();

        ImmutablePair<Type, Object> ans;
        for (IArgParser parse : parseList) {
            ans = parse.parse(type, value);
            if (ans != null) {
                return ans;
            }
        }

        throw new IllegalInvokeArgumentException("Pare Argument to Object Error! type: " + type + " value: " + value);
    }

    private static void loadParser() {
        if (parseList == null) {
            synchronized (ArgumentParser.class) {
                if (parseList == null) {
                    List<IArgParser> tmpParseList = new ArrayList<>(12);
                    ServiceLoader<IArgParser> list = ServiceLoader.load(IArgParser.class);
                    for (IArgParser parse : list) {
                        tmpParseList.add(parse);
                    }
                    tmpParseList.sort(null);
                    parseList = tmpParseList;
                }
            }
        }
    }
}
