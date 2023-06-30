package com.example.helloworld.dto;

import java.time.Instant;
import java.util.Objects;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonCreator;

public class PersonDTO
{
    private Long    id;
    private String  fullName;
    private String  jobTitle;
    @Min(value = 0)
    @Max(value = 9999)
    private Integer yearBorn;
    private Instant system;
    private Instant systemFrom;
    private Instant systemTo;

    @JsonCreator
    public PersonDTO()
    {
    }

    public PersonDTO(
            String fullName,
            String jobTitle,
            Integer yearBorn)
    {
        this.fullName = Objects.requireNonNull(fullName);
        this.jobTitle = Objects.requireNonNull(jobTitle);
        this.yearBorn = Objects.requireNonNull(yearBorn);
    }

    public PersonDTO(
            long id,
            String fullName,
            String jobTitle,
            Integer yearBorn)
    {
        this(fullName, jobTitle, yearBorn);
        this.id       = id;
    }

    public Long getId()
    {
        return this.id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getFullName()
    {
        return this.fullName;
    }

    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }

    public String getJobTitle()
    {
        return this.jobTitle;
    }

    public void setJobTitle(String jobTitle)
    {
        this.jobTitle = jobTitle;
    }

    public Integer getYearBorn()
    {
        return yearBorn;
    }

    public void setYearBorn(Integer yearBorn)
    {
        this.yearBorn = yearBorn;
    }

    public Instant getSystem()
    {
        return this.system;
    }

    public void setSystem(Instant system)
    {
        this.system = system;
    }

    public Instant getSystemFrom()
    {
        return this.systemFrom;
    }

    public void setSystemFrom(Instant systemFrom)
    {
        this.systemFrom = systemFrom;
    }

    public Instant getSystemTo()
    {
        return this.systemTo;
    }

    public void setSystemTo(Instant systemTo)
    {
        this.systemTo = systemTo;
    }

    @Override
    public String toString()
    {
        return "PersonDTO{" +
                "id=" + this.id +
                ", fullName='" + this.fullName + '\'' +
                ", jobTitle='" + this.jobTitle + '\'' +
                ", system=" + this.system +
                ", systemFrom=" + this.systemFrom +
                ", systemTo=" + this.systemTo +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || this.getClass() != o.getClass())
        {
            return false;
        }

        PersonDTO personDTO = (PersonDTO) o;

        if (!Objects.equals(this.id, personDTO.id))
        {
            return false;
        }
        if (!Objects.equals(this.fullName, personDTO.fullName))
        {
            return false;
        }
        if (!Objects.equals(this.jobTitle, personDTO.jobTitle))
        {
            return false;
        }
        if (!Objects.equals(this.system, personDTO.system))
        {
            return false;
        }
        if (!Objects.equals(this.systemFrom, personDTO.systemFrom))
        {
            return false;
        }
        return Objects.equals(this.systemTo, personDTO.systemTo);
    }

    @Override
    public int hashCode()
    {
        int result = Objects.hashCode(this.id);
        result = 31 * result + Objects.hashCode(this.fullName);
        result = 31 * result + Objects.hashCode(this.jobTitle);
        result = 31 * result + Objects.hashCode(this.system);
        result = 31 * result + Objects.hashCode(this.systemFrom);
        result = 31 * result + Objects.hashCode(this.systemTo);
        return result;
    }
}
