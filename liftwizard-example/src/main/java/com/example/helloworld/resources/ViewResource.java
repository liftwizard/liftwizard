package com.example.helloworld.resources;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

import io.dropwizard.views.View;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/views")
public class ViewResource {

	@GET
	@Produces("text/html;charset=UTF-8")
	@Path("/utf8.ftl")
	public View freemarkerUTF8() {
		return new View("/views/ftl/utf8.ftl", UTF_8) {};
	}

	@GET
	@Produces("text/html;charset=ISO-8859-1")
	@Path("/iso88591.ftl")
	public View freemarkerISO88591() {
		return new View("/views/ftl/iso88591.ftl", ISO_8859_1) {};
	}

	@GET
	@Produces("text/html;charset=UTF-8")
	@Path("/utf8.mustache")
	public View mustacheUTF8() {
		return new View("/views/mustache/utf8.mustache", UTF_8) {};
	}

	@GET
	@Produces("text/html;charset=ISO-8859-1")
	@Path("/iso88591.mustache")
	public View mustacheISO88591() {
		return new View("/views/mustache/iso88591.mustache", ISO_8859_1) {};
	}
}
