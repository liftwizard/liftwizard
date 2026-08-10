import io.dropwizard.auth.AuthFilter;
import io.dropwizard.jersey.setup.JerseyEnvironment;

class MyResource {
	AuthFilter filter;
	JerseyEnvironment jersey;
}
