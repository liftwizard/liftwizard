package com.liftwizard.dropwizard.configuration.clock;

import java.time.Clock;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.google.auto.service.AutoService;
import io.dropwizard.jackson.Discoverable;

@JsonTypeInfo(use = Id.NAME, property = "type")
@AutoService(Discoverable.class)
public interface ClockFactory extends Discoverable
{
    Clock createClock();
}
