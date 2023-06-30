package com.example.helloworld.db;

import java.util.Optional;

import com.example.helloworld.core.Person;
import com.example.helloworld.core.PersonFinder;
import com.example.helloworld.core.PersonList;
import com.gs.fw.common.mithra.MithraManagerProvider;

public class PersonDAO {
    public Optional<Person> findById(Long id) {
        return Optional.ofNullable(PersonFinder.findOne(PersonFinder.id().eq(id)));
    }

    public Person create(Person person) {
        return MithraManagerProvider.getMithraManager().executeTransactionalCommand(tx ->
        {
            person.insert();
            return person;
        });
    }

    public PersonList findAll() {
        return PersonFinder.findMany(PersonFinder.all());
    }
}
