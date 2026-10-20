package com.example.helloworld.core;

import java.sql.Timestamp;

import io.liftwizard.reladomo.utc.infinity.timestamp.UtcInfinityTimestamp;

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
		this(UtcInfinityTimestamp.getDefaultInfinity());
	}

	public Person(String fullName, String jobTitle)
	{
		this();
		this.setFullName(fullName);
		this.setJobTitle(jobTitle);
	}
}
