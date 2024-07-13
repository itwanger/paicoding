package com.github.paicoding.forum.test.javabetter.socket1.httpserver;

import lombok.Getter;
import lombok.Setter;
import ognl.ClassResolver;
import ognl.MemberAccess;
import ognl.OgnlContext;
import ognl.TypeConverter;

import java.util.List;
import java.util.Map;

public class OgnlContextExtend extends OgnlContext {
    @Getter
    @Setter
    private List<ServerLoader> serverLoaders;

    public OgnlContextExtend(ClassResolver classResolver, TypeConverter typeConverter, MemberAccess memberAccess) {
        super(classResolver, typeConverter, memberAccess);
    }

    public OgnlContextExtend(MemberAccess memberAccess, ClassResolver classResolver, TypeConverter typeConverter,
                             Map values) {
        super(memberAccess, classResolver, typeConverter, values);
    }

    @Override
    public Object get(Object key) {
        Object result = super.get(key);
        if (result != null) {
            return result;
        }

        for (ServerLoader loader : serverLoaders) {
            result = loader.getInvokeObject((String) key);
            if (result != null) {
                put(key, result);
                return result;
            }
        }
        return null;
    }
}