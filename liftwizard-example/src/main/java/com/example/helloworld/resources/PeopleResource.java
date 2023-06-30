package com.example.helloworld.resources;

import java.util.List;

import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.example.helloworld.core.Person;
import com.example.helloworld.db.PersonDAO;
import com.example.helloworld.dto.PersonDTO;
import io.dropwizard.hibernate.UnitOfWork;
import org.eclipse.collections.impl.list.mutable.ListAdapter;

@Path("/people")
@Produces(MediaType.APPLICATION_JSON)
public class PeopleResource {

    private final PersonDAO peopleDAO;

    public PeopleResource(PersonDAO peopleDAO) {
        this.peopleDAO = peopleDAO;
    }

    @POST
    @UnitOfWork
    public PersonDTO createPerson(@Valid PersonDTO personDTO) {
        Person person = new Person();
        person.setFullName(personDTO.getFullName());
        person.setJobTitle(personDTO.getJobTitle());
        person.setYearBorn(personDTO.getYearBorn());
        Person result = this.peopleDAO.create(person);
        return new PersonDTO(result.getId(), result.getFullName(), result.getJobTitle(), result.getYearBorn());
    }

    @GET
    @UnitOfWork
    public List<PersonDTO> listPeople() {
        return ListAdapter.adapt(this.peopleDAO.findAll())
                .collect(each -> new PersonDTO(
                        each.getId(),
                        each.getFullName(),
                        each.getJobTitle(),
                        each.getYearBorn()));
    }
}
