package com.github.paicoding.forum.test.javabetter.io1;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class KryoDemo {
    public static void main(String[] args) throws FileNotFoundException {
        Kryo kryo = new Kryo();
        kryo.register(KryoParam.class);

        KryoParam object = new KryoParam("沉默王二", 123);

        Output output = new Output(new FileOutputStream("logs/kryo.bin"));
        kryo.writeObject(output, object);
        output.close();

        Input input = new Input(new FileInputStream("logs/kryo.bin"));
        KryoParam object2 = kryo.readObject(input, KryoParam.class);
        System.out.println(object2);
        input.close();
    }
}

class KryoParam {
    private String name;
    private int age;

    public KryoParam() {
    }

    public KryoParam(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "KryoParam{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}