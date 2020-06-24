package com.example.helloworld.core;

import java.sql.Timestamp;

import com.gs.fw.common.mithra.util.DefaultInfinityTimestamp;

public class Person
        extends PersonAbstract
{
    public Person(Timestamp system)
    {
        super(system);
        // You must not modify this constructor. Mithra calls this internally.
        // You can call this constructor. You can also add new constructors.
    }

    public Person()
    {
        this(DefaultInfinityTimestamp.getDefaultInfinity());
    }

    public Person(String fullName, String jobTitle)
    {
        this();
        this.setFullName(fullName);
        this.setJobTitle(jobTitle);
    }
}
