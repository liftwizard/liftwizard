package com.liftwizard.servlet.logging.resource.info;

import javax.annotation.Nonnull;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import com.liftwizard.logging.slf4j.mdc.MultiMDCCloseable;
import org.eclipse.collections.impl.list.mutable.ListAdapter;
import org.glassfish.jersey.server.ContainerRequest;

// Priority must be less than the priority of StructuredArgumentLoggingFilter
@Provider
@Priority(Priorities.USER - 20)
public class ResourceInfoLoggingFilter implements ContainerRequestFilter
{
    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(@Nonnull ContainerRequestContext requestContext)
    {
        MultiMDCCloseable mdc = (MultiMDCCloseable) requestContext.getProperty("mdc");

        UriInfo uriInfo = requestContext.getUriInfo();

        String httpPath           = uriInfo.getPath();
        String httpMethod         = requestContext.getMethod();
        String resourceClassName  = this.resourceInfo.getResourceClass().getCanonicalName();
        String resourceMethodName = this.resourceInfo.getResourceMethod().getName();

        mdc.put("liftwizard.request.httpPath", httpPath);
        mdc.put("liftwizard.request.httpMethod", httpMethod);
        mdc.put("liftwizard.request.resourceClassName", resourceClassName);
        mdc.put("liftwizard.request.resourceMethodName", resourceMethodName);

        uriInfo.getQueryParameters().forEach((parameterName, parameterValues) ->
        {
            String key   = "liftwizard.request.parameter.query." + parameterName;
            String value = ListAdapter.adapt(parameterValues).makeString();
            mdc.put(key, value);
        });

        uriInfo.getPathParameters().forEach((parameterName, parameterValues) ->
        {
            String key   = "liftwizard.request.parameter.path." + parameterName;
            String value = ListAdapter.adapt(parameterValues).makeString();
            mdc.put(key, value);
        });

        if (requestContext instanceof ContainerRequest)
        {
            ContainerRequest containerRequest = (ContainerRequest) requestContext;
            String           pathTemplate     = containerRequest.getUriInfo().getMatchedModelResource().getPath();
            mdc.put("liftwizard.request.httpPathTemplate", pathTemplate);
        }
    }
}
