package com.github.paicoding.forum.test.javabetter.yufa;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 4/9/23
 */
public class WrapperDemo {
    public static void main(String[] args) {
        // 使用 Integer 包装器类型
        Integer integerValue = new Integer(42);
        System.out.println("Integer value: " + integerValue);

        // 将字符串转换为整数
        String numberString = "123";
        int parsedNumber = Integer.parseInt(numberString);
        System.out.println("Parsed number: " + parsedNumber);

        // 使用 Character 包装器类型
        Character charValue = new Character('A');
        System.out.println("Character value: " + charValue);

        // 检查字符是否为数字
        char testChar = '9';
        if (Character.isDigit(testChar)) {
            System.out.println("The character is a digit.");
        }
    }
}
