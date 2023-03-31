package com.github.paicoding.forum.test.javabetter.socket1.httpserver;

import lombok.Setter;
import ognl.MemberAccess;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Map;

@Setter
public class DefaultMemberAccess implements MemberAccess {
    private boolean allowPrivateAccess = false;
    private boolean allowProtectedAccess = false;
    private boolean allowPackageProtectedAccess = false;

    public DefaultMemberAccess(boolean allowAllAccess) {
        this(allowAllAccess, allowAllAccess, allowAllAccess);
    }

    public DefaultMemberAccess(boolean allowPrivateAccess, boolean allowProtectedAccess,
                               boolean allowPackageProtectedAccess) {
        super();
        this.allowPrivateAccess = allowPrivateAccess;
        this.allowProtectedAccess = allowProtectedAccess;
        this.allowPackageProtectedAccess = allowPackageProtectedAccess;
    }

    @Override
    public Object setup(Map context, Object target, Member member, String propertyName) {
        Object result = null;

        if (isAccessible(context, target, member, propertyName)) {
            AccessibleObject accessible = (AccessibleObject) member;

            if (!accessible.isAccessible()) {
                result = Boolean.TRUE;
                accessible.setAccessible(true);
            }
        }
        return result;
    }

    @Override
    public void restore(Map context, Object target, Member member, String propertyName, Object state) {
        if (state != null) {
            ((AccessibleObject) member).setAccessible((Boolean) state);
        }
    }

    /**
     * Returns true if the given member is accessible or can be made accessible by this object.
     */
    @Override
    public boolean isAccessible(Map context, Object target, Member member, String propertyName) {
        int modifiers = member.getModifiers();
        if (Modifier.isPublic(modifiers)) {
            return true;
        } else if (Modifier.isPrivate(modifiers)) {
            return this.allowPrivateAccess;
        } else if (Modifier.isProtected(modifiers)) {
            return this.allowProtectedAccess;
        } else {
            return this.allowPackageProtectedAccess;
        }
    }
}
