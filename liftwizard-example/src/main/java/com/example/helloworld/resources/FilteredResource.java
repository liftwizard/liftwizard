package com.example.helloworld.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.example.helloworld.filter.DateRequired;

@Path("/filtered")
public class FilteredResource {

    @GET
    @DateRequired
    @Path("hello")
    public String sayHello() {
        return "hello";
    }
}
