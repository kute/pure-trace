package com.kute.pureagent.bytebuddy;

public class Foo {
    public String sayHelloFoo() {
        return "sayHelloFoo";
    }

    public String say(String name) {
        return "say " + name;
    }

    public String say(String name, String age) {
        return "say " + name + age;
    }
}