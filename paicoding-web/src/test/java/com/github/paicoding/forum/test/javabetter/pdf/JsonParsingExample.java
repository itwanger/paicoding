package com.github.paicoding.forum.test.javabetter.pdf;

import com.google.gson.*;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 4/10/23
 */
public class JsonParsingExample {
    public static void main(String[] args) {
        String jsonString = "[\n" +
                "  {\n" +
                "    \"prefix\": \"overview/\",\n" +
                "    \"text\": \"2.1 Java概述及环境配置\",\n" +
                "    \"collapsible\": true,\n" +
                "    \"children\": [\n" +
                "      \"readme.md\",\n" +
                "      \"what-is-java\",\n" +
                "      \"jdk-install-config\",\n" +
                "      \"IDEA-install-config\",\n" +
                "      \"hello-world\"\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"text\": \"2.2 Java语法基础\",\n" +
                "    \"collapsible\": true,\n" +
                "    \"children\": [\n" +
                "      \"basic-extra-meal/48-keywords\",\n" +
                "      \"basic-grammar/javadoc\",\n" +
                "      \"basic-grammar/basic-data-type\",\n" +
                "      \"basic-grammar/type-cast\",\n" +
                "      \"basic-extra-meal/int-cache\",\n" +
                "      \"basic-grammar/operator\",\n" +
                "      \"basic-grammar/flow-control\"\n" +
                "    ]\n" +
                "  }\n" +
                "]";


        JsonArray jsonArray = JsonParser.parseString(jsonString).getAsJsonArray();

        Gson gson = new Gson();
        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            System.out.println(gson.toJson(jsonObject));
        }
    }
}
