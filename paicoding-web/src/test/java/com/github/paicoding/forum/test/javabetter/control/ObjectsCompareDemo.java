package com.github.paicoding.forum.test.javabetter.control;

import java.util.Comparator;
import java.util.Objects;

public class ObjectsCompareDemo {
    public static void main(String[] args) {
        PersonCompare person1 = new PersonCompare("itwanger", 30);
        PersonCompare person2 = new PersonCompare("chenqingyang", 25);

        Comparator<PersonCompare> ageComparator = Comparator.comparingInt(p -> p.age);
        int ageComparisonResult = Objects.compare(person1, person2, ageComparator);
        System.out.println("年龄排序: " + ageComparisonResult); // 输出：1（表示 person1 的 age 在 person2 之后）
    }
}

class PersonCompare {
    String name;
    int age;

    PersonCompare(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
