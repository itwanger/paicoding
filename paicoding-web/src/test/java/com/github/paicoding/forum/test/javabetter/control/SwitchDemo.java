package com.github.paicoding.forum.test.javabetter.control;

public class SwitchDemo {
    public static void main(String[] args) {
        // switch 语句中的表达式类型可以是 byte、short、int 或者 char
        // 从 Java SE 7 开始，switch 支持字符串 String 类型了
        // 还有枚举
        // case 标签必须为整型常量或者枚举类型
        // case 标签中的常量值不能重复
        // switch 语句可以没有 default 语句
        Character num = 2;
        switch (num) {
            case 1:
                System.out.println("num = 1");
                break;
            case 2:
                System.out.println("num = 2");
                break;
            case 3:
                System.out.println("num = 3");
                break;
            default:
                System.out.println("num 不是 1、2 或 3");
                break;
        }
    }
}
