package com.example.helloworld.core;

import com.gs.fw.finder.Operation;
import java.util.Collection;

public class PersonList extends PersonListAbstract {

    public PersonList() {}

    public PersonList(int initialSize) {
        super(initialSize);
    }

    public PersonList(Collection c) {
        super(c);
    }

    public PersonList(Operation operation) {
        super(operation);
    }
}
