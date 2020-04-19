package com.liftwizard.dropwizard.healthcheck.reladomo;

import com.codahale.metrics.health.HealthCheck;
import com.liftwizard.reladomo.simseq.ObjectSequenceFinder;
import com.liftwizard.reladomo.simseq.ObjectSequenceList;

public class ReladomoHealthCheck extends HealthCheck
{
    @Override
    protected Result check()
    {
        ObjectSequenceList objectSequences = ObjectSequenceFinder.findMany(ObjectSequenceFinder.all());
        int                size            = objectSequences.size();
        return Result.healthy("Found " + size + " rows.");
    }
}
