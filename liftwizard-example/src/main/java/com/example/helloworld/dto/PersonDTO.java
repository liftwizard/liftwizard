package com.example.helloworld.dto;

import java.time.Instant;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;

public class PersonDTO
{
    private Long    id;
    @NotNull
    private String  fullName;
    @NotNull
    private String  jobTitle;
    private Instant system;
    private Instant systemFrom;
    private Instant systemTo;

    @JsonCreator
    public PersonDTO()
    {
    }

    public PersonDTO(
            @NotNull String fullName,
            @NotNull String jobTitle)
    {
        this.fullName = Objects.requireNonNull(fullName);
        this.jobTitle = Objects.requireNonNull(jobTitle);
    }

    public PersonDTO(
            long id,
            @NotNull String fullName,
            @NotNull String jobTitle)
    {
        this.id       = id;
        this.fullName = Objects.requireNonNull(fullName);
        this.jobTitle = Objects.requireNonNull(jobTitle);
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
}
