package com.liftwizard.servlet.logging.structured.reladomo;

import java.util.Map;
import java.util.Objects;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import com.gs.fw.common.mithra.MithraManagerProvider;

public class ReladomoStructuredLoggingFilter implements ClientRequestFilter, ContainerResponseFilter
{
    public static final String STRUCTURED_ARGUMENTS_PROPERTY_NAME    = "structuredArguments";
    public static final String REMOTE_RETRIEVE_COUNT_PROPERTY_NAME   = "remoteRetrieveCount";
    public static final String DATABASE_RETRIEVE_COUNT_PROPERTY_NAME = "databaseRetrieveCount";

    private final String structuredArgumentsPropertyName;
    private final String remoteRetrieveCountPropertyName;
    private final String databaseRetrieveCountPropertyName;

    public ReladomoStructuredLoggingFilter()
    {
        this(
                STRUCTURED_ARGUMENTS_PROPERTY_NAME,
                REMOTE_RETRIEVE_COUNT_PROPERTY_NAME,
                DATABASE_RETRIEVE_COUNT_PROPERTY_NAME);
    }

    public ReladomoStructuredLoggingFilter(
            String structuredArgumentsPropertyName,
            String remoteRetrieveCountPropertyName,
            String databaseRetrieveCountPropertyName)
    {
        this.structuredArgumentsPropertyName   = Objects.requireNonNull(structuredArgumentsPropertyName);
        this.remoteRetrieveCountPropertyName   = Objects.requireNonNull(remoteRetrieveCountPropertyName);
        this.databaseRetrieveCountPropertyName = Objects.requireNonNull(databaseRetrieveCountPropertyName);
    }

    @Override
    public void filter(ClientRequestContext requestContext)
    {
        int remoteRetrieveCount   = MithraManagerProvider.getMithraManager().getRemoteRetrieveCount();
        int databaseRetrieveCount = MithraManagerProvider.getMithraManager().getDatabaseRetrieveCount();
        requestContext.setProperty(this.remoteRetrieveCountPropertyName, remoteRetrieveCount);
        requestContext.setProperty(this.databaseRetrieveCountPropertyName, databaseRetrieveCount);
    }

    @Override
    public void filter(
            ContainerRequestContext requestContext,
            ContainerResponseContext responseContext)
    {
        Object structuredArguments = requestContext.getProperty(this.structuredArgumentsPropertyName);
        Objects.requireNonNull(structuredArguments);
        Map<String, Object> structuredArgumentsMap = (Map<String, Object>) structuredArguments;

        Object remoteRetrieveCountBeforeObject = requestContext.getProperty(this.remoteRetrieveCountPropertyName);
        if (remoteRetrieveCountBeforeObject != null)
        {
            int remoteRetrieveCountBefore = (int) remoteRetrieveCountBeforeObject;
            int remoteRetrieveCountAfter  = MithraManagerProvider.getMithraManager().getRemoteRetrieveCount();
            int remoteRetrieveCount       = remoteRetrieveCountAfter - remoteRetrieveCountBefore;
            structuredArgumentsMap.put("liftwizard.response.reladomo.remoteRetrieveCount", remoteRetrieveCount);
        }

        Object databaseRetrieveCountBeforeObject = requestContext.getProperty(this.databaseRetrieveCountPropertyName);
        if (databaseRetrieveCountBeforeObject != null)
        {
            int databaseRetrieveCountBefore = (int) databaseRetrieveCountBeforeObject;
            int databaseRetrieveCountAfter  = MithraManagerProvider.getMithraManager().getDatabaseRetrieveCount();
            int databaseRetrieveCount       = databaseRetrieveCountAfter - databaseRetrieveCountBefore;
            structuredArgumentsMap.put("liftwizard.response.reladomo.databaseRetrieveCount", databaseRetrieveCount);
        }
    }
}
