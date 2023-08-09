package com.github.paicoding.forum.core.senstive.ibatis;
import com.github.paicoding.forum.core.senstive.ano.SensitiveField;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * 敏感词相关配置，db配置表中的配置优先级更高，支持动态刷新
 *
 * @author YiHui
 * @date 2023/8/9
 */
@Data
public class SensitiveObjectMeta {
    private static final String JAVA_LANG_OBJECT = "java.lang.object";
    /**
     * 是否启用脱敏
     */
    private Boolean enabledSensitiveReplace;

    private String className;

    private List<SensitiveFieldMeta> sensitiveFieldMetaList;
    private List<SensitiveBindMeta> sensitiveBindFieldMetaList;

    private SensitiveObjectMeta() {
        sensitiveFieldMetaList = newArrayList();
        sensitiveBindFieldMetaList = newArrayList();
    }

    public static Optional<SensitiveObjectMeta> generateSensitiveObjectMeta(Object param) {
        if (isNull(param)) {
            return Optional.empty();
        }

        Class<?> clazz = param.getClass();
        SensitiveObjectMeta sensitiveObjectMeta = new SensitiveObjectMeta();
        sensitiveObjectMeta.setClassName(clazz.getName());

        boolean sensitiveField = hasSensitiveField(clazz);
        sensitiveObjectMeta.setEnabledSensitiveReplace(sensitiveField);
        if (sensitiveField) {
            List<SensitiveFieldMeta> sensitiveFieldMetaList = newArrayList();
            List<SensitiveBindMeta> sensitiveBindFieldMetaList = newArrayList();
            sensitiveObjectMeta.setSensitiveFieldMetaList(sensitiveFieldMetaList);
            sensitiveObjectMeta.setSensitiveBindFieldMetaList(sensitiveBindFieldMetaList);

            parseAllSensitiveFields(clazz, sensitiveFieldMetaList, sensitiveBindFieldMetaList);
        }

        return Optional.of(sensitiveObjectMeta);
    }

    private static boolean hasSensitiveField(Class clz) {
        for (Field f: clz.getDeclaredFields()) {
            if (f.getAnnotation(SensitiveField.class) != null) {
                return true;
            }
        }
        return false;
    }

    private static void parseAllSensitiveFields(Class<?> clazz, List<SensitiveFieldMeta> sensitiveFieldMetaList, List<SensitiveBindMeta> sensitiveBindFieldMetaList) {
        Class<?> tempClazz = clazz;
        while (nonNull(tempClazz) && !JAVA_LANG_OBJECT.equalsIgnoreCase(tempClazz.getName())) {
            for (Field field : tempClazz.getDeclaredFields()) {
                if (String.class.equals(field.getType())) {

                    SensitiveField sensitiveField = field.getAnnotation(SensitiveField.class);
                    if (nonNull(sensitiveField)) {
                        if (StringUtils.isEmpty(sensitiveField.bind())) {
                            SensitiveFieldMeta sensitiveFieldMeta = new SensitiveFieldMeta();
                            sensitiveFieldMeta.setName(field.getName());
                            sensitiveFieldMetaList.add(sensitiveFieldMeta);
                        } else {
                            SensitiveBindMeta sensitiveBindFieldMeta = new SensitiveBindMeta();
                            sensitiveBindFieldMeta.setName(field.getName());
                            sensitiveBindFieldMeta.setBindField(sensitiveField.bind());
                            sensitiveBindFieldMetaList.add(sensitiveBindFieldMeta);
                        }
                    }
                } else if (field.getType().isAssignableFrom(List.class)) {
                    SensitiveField sensitiveField = field.getAnnotation(SensitiveField.class);
                    if (nonNull(sensitiveField)) {
                        SensitiveFieldMeta sensitiveFieldMeta = new SensitiveFieldMeta();
                        sensitiveFieldMeta.setName(field.getName());
                        sensitiveFieldMetaList.add(sensitiveFieldMeta);
                    }
                } else if (nonNull(field.getType().getAnnotation(SensitiveField.class))) {
                    SensitiveField sensitiveField = field.getAnnotation(SensitiveField.class);
                    if (nonNull(sensitiveField)) {
                        SensitiveFieldMeta sensitiveFieldMeta = new SensitiveFieldMeta();
                        sensitiveFieldMeta.setName(field.getName());
                        sensitiveFieldMetaList.add(sensitiveFieldMeta);
                    }
                }
            }
            tempClazz = tempClazz.getSuperclass();
        }
    }


    @Data
    public static class SensitiveFieldMeta {
        private String name;
    }

    @Data
    public static class SensitiveBindMeta {
        private String name;

        private String bindField;
    }
}
