package org.obj;

import com.rabbani.sp.annotation.Discriminable;
import com.rabbani.sp.annotation.Traverse;

import java.util.List;

@Discriminable
public class Person {
    String name;
    int age;
    @Traverse
    List<Person> siblings;

    public Person(String name, int age, List<Person> siblings) {
        this.name = name;
        this.age = age;
        this.siblings = siblings;
    }
}
