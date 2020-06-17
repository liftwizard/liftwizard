package com.example.helloworld.resources;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.example.helloworld.core.Person;
import com.example.helloworld.db.PersonDAO;
import com.example.helloworld.dto.PersonDTO;
import io.dropwizard.hibernate.UnitOfWork;

@Path("/people")
@Produces(MediaType.APPLICATION_JSON)
public class PeopleResource {

    private final PersonDAO peopleDAO;

    public PeopleResource(PersonDAO peopleDAO) {
        this.peopleDAO = peopleDAO;
    }

    @POST
    @UnitOfWork
    public PersonDTO createPerson(Person person) {
        Person result = peopleDAO.create(person);
        return new PersonDTO(result.getId(), result.getFullName(), result.getJobTitle());
    }

    @GET
    @UnitOfWork
    public List<PersonDTO> listPeople() {
        return this.peopleDAO.findAll()
                .asEcList()
                .collect(each -> new PersonDTO(
                        each.getId(),
                        each.getFullName(),
                        each.getJobTitle()));
    }

}
