package com.example.helloworld.core;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.gs.fw.common.mithra.util.DefaultInfinityTimestamp;

@JsonIncludeProperties({"fullName", "jobTitle", "yearBorn"})
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

    public Person(String fullName, String jobTitle, int yearBorn)
    {
        this();
        this.setFullName(fullName);
        this.setJobTitle(jobTitle);
        this.setYearBorn(yearBorn);
    }

    @Override
    public String toString()
    {
        String format = ""
                + "{\n" +
                "  \"fullName\": \"%s\",\n" +
                "  \"jobTitle\": \"%s\",\n" +
                "  \"yearBorn\": \"%d\"\n" +
                "}\n";
        return String.format(
                format,
                getFullName(),
                getJobTitle(),
                getYearBorn());
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof Person && this.getFullName().equals(((Person) obj).getFullName())
                && this.getJobTitle().equals(((Person) obj).getJobTitle())
                && this.getYearBorn() == ((Person) obj).getYearBorn();
    }

    @Override
    public int hashCode()
    {
        int result = 17;
        result = 31 * result + this.getFullName().hashCode();
        result = 31 * result + this.getJobTitle().hashCode();
        result = 31 * result + this.getYearBorn();
        return result;
    }
}
