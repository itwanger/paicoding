package com.github.paicoding.forum.test.javabetter.io1;

import java.io.PrintStream;

public class PrintStreamDemo1 {
    public static void main(String[] args) {
        PrintStream ps = System.out;
        ps.println("沉默王二");
        ps.print("沉 ");
        ps.print("默 ");
        ps.print("王 ");
        ps.print("二 ");
        ps.println();

        ps.printf("姓名：%s，年龄：%d，成绩：%f", "沉默王二", 18, 99.9);

        // 下面是 Java 的常用转换说明符及对应的输出格式，给出示例
        // %b：boolean 类型
        // %d：整数类型（十进制）
        // %f：浮点类型
        // %s：字符串类型
        // %x：整数类型（十六进制）
        // %n：换行符
        // %c：字符类型
        // %e：指数类型
        // %g：通用浮点类型（f和e类型中较短的）
        // %%：百分比类型
        // %h：散列码
        // %o：八进制整数
        // %t：日期与时间类型（%tY:四位年份，%ty:两位年份，%tm:两位月份，%td:两位日期，%tH:两位小时，%tM:两位分钟，%tS:两位秒）
        // %T：24时制的时间
        // %r：12时制的时间
        // %tc：日期与时间类型（如：2003-10-18 22:10:28）
        // %tx：日期类型（如：2003-10-18）
        // %tR：24时制的时间（如：22:10）
        // %tr：12时制的时间（如：10:10:28 下午）
        // %tD：日期类型（如：10/18/03）
        // %tF：ISO 8601标准日期格式（如：2003-10-18）
        // %tT：24时制的时间（如：22:10:28）
        // %tz：时区（如：中国标准时间）
        // %tZ：时区（如：CST）
        // %s：字符串类型
        // %S：字符串类型
        // %n：换行符
        // %p：百分比类型
        // %a：十六进制浮点数
        // %A：十六进制浮点数
        // %h：散列码

        int num = 123;
        System.out.printf("%5d\n", num); // 输出 "  123"
        System.out.printf("%-5d\n", num); // 输出 "123  "
        System.out.printf("%05d\n", num); // 输出 "00123"

        double pi = Math.PI;
        System.out.printf("%10.2f\n", pi); // 输出 "      3.14"
        System.out.printf("%-10.4f\n", pi); // 输出 "3.1416    "

        String name = "沉默王二";
        System.out.printf("%10s\n", name); // 输出 "     沉默王二"
        System.out.printf("%-10s\n", name); // 输出 "沉默王二     "

    }
}
