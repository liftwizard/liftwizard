package com.example.helloworld.resources;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.example.helloworld.core.Person;
import com.example.helloworld.core.PersonList;
import com.example.helloworld.db.PersonDAO;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PeopleResource}.
 */
@ExtendWith(DropwizardExtensionsSupport.class)
class PeopleResourceTest {
    private static final PersonDAO PERSON_DAO = mock(PersonDAO.class);
    public static final ResourceExtension RESOURCES = ResourceExtension.builder()
            .addResource(new PeopleResource(PERSON_DAO))
            .build();
    private final ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);

    private final Person person = new Person("Full Name", "Job Title", 1995);

    @BeforeEach
    void setUp() {
        when(PERSON_DAO.create(any(Person.class))).thenReturn(person);
    }

    @AfterEach
    void tearDown() {
        reset(PERSON_DAO);
    }

    @Test
    void createPerson() {
        final Response response = RESOURCES.target("/people")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(person, MediaType.APPLICATION_JSON_TYPE));

        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.OK);
        verify(PERSON_DAO).create(personCaptor.capture());
        assertThat(personCaptor.getValue()).isEqualTo(person);
    }

    @Test
    void createPersonFailureMinYearBorn() {
        person.setYearBorn(-1);

        final Response response = RESOURCES.target("/people")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(person, MediaType.APPLICATION_JSON_TYPE));

        String actualResponse = response.readEntity(String.class);
        assertThat(response.getStatusInfo()).isNotEqualTo(Response.Status.OK).describedAs(actualResponse);
        assertThat(actualResponse).contains("yearBorn must be greater than or equal to 0");
    }

    @Test
    void createPersonFailureMaxYearBorn() {
        person.setYearBorn(10000);

        final Response response = RESOURCES.target("/people")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(person, MediaType.APPLICATION_JSON_TYPE));

        assertThat(response.getStatusInfo()).isNotEqualTo(Response.Status.OK);
        assertThat(response.readEntity(String.class)).contains("yearBorn must be less than or equal to 9999");
    }

    @Test
    void listPeople() {
        final PersonList people = new PersonList();
        people.add(person);
        when(PERSON_DAO.findAll()).thenReturn(people);

        final PersonList response = RESOURCES.target("/people")
                .request().get(new GenericType<>() {

                });

        verify(PERSON_DAO).findAll();
        assertThat(response).containsAll(people);
    }
}
