package com.example.helloworld.core;

import java.util.Collection;

import com.gs.fw.finder.Operation;

public class PersonList
        extends PersonListAbstract
{
    public PersonList()
    {
    }

    public PersonList(int initialSize)
    {
        super(initialSize);
    }

    public PersonList(Collection c)
    {
        super(c);
    }

    public PersonList(Operation operation)
    {
        super(operation);
    }
}
