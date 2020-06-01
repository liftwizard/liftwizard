package com.example.helloworld;

import java.util.EnumSet;
import java.util.Map;

import javax.servlet.DispatcherType;

import com.example.helloworld.auth.ExampleAuthenticator;
import com.example.helloworld.auth.ExampleAuthorizer;
import com.example.helloworld.cli.RenderCommand;
import com.example.helloworld.core.Person;
import com.example.helloworld.core.Template;
import com.example.helloworld.core.User;
import com.example.helloworld.db.PersonDAO;
import com.example.helloworld.filter.DateRequiredFeature;
import com.example.helloworld.health.TemplateHealthCheck;
import com.example.helloworld.resources.FilteredResource;
import com.example.helloworld.resources.HelloWorldResource;
import com.example.helloworld.resources.PeopleResource;
import com.example.helloworld.resources.PersonResource;
import com.example.helloworld.resources.ProtectedResource;
import com.example.helloworld.resources.ViewResource;
import com.example.helloworld.tasks.EchoTask;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import io.liftwizard.dropwizard.bundle.clock.ClockBundle;
import io.liftwizard.dropwizard.bundle.config.logging.ConfigLoggingBundle;
import io.liftwizard.dropwizard.bundle.environment.config.EnvironmentConfigBundle;
import io.liftwizard.dropwizard.bundle.httplogging.JerseyHttpLoggingBundle;
import io.liftwizard.dropwizard.bundle.objectmapper.ObjectMapperBundle;
import io.liftwizard.dropwizard.bundle.uuid.UUIDBundle;
import io.liftwizard.dropwizard.configuration.factory.JsonConfigurationFactoryFactory;
import io.liftwizard.servlet.logging.correlation.id.CorrelationIdFilter;
import io.liftwizard.servlet.logging.resource.info.ResourceInfoLoggingFilter;
import io.liftwizard.servlet.logging.structured.argument.StructuredArgumentLoggingFilter;
import io.liftwizard.servlet.logging.structured.duration.DurationStructuredLoggingFilter;
import io.liftwizard.servlet.logging.structured.status.info.StatusInfoStructuredLoggingFilter;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

public class HelloWorldApplication extends Application<HelloWorldConfiguration> {
    public static void main(String[] args) throws Exception {
        new HelloWorldApplication().run(args);
    }

    private final HibernateBundle<HelloWorldConfiguration> hibernateBundle =
        new HibernateBundle<HelloWorldConfiguration>(Person.class) {
            @Override
            public DataSourceFactory getDataSourceFactory(HelloWorldConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        };

    @Override
    public String getName() {
        return "hello-world";
    }

    @Override
    public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {
        bootstrap.setConfigurationFactoryFactory(new JsonConfigurationFactoryFactory<>());
        bootstrap.addBundle(new EnvironmentConfigBundle());

        bootstrap.addBundle(new ObjectMapperBundle());
        bootstrap.addBundle(new ConfigLoggingBundle());

        bootstrap.addBundle(new JerseyHttpLoggingBundle());

        bootstrap.addBundle(new ClockBundle());
        bootstrap.addBundle(new UUIDBundle());

        bootstrap.addCommand(new RenderCommand());
        bootstrap.addBundle(new AssetsBundle());
        bootstrap.addBundle(new MigrationsBundle<HelloWorldConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(HelloWorldConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        });
        bootstrap.addBundle(hibernateBundle);
        bootstrap.addBundle(new ViewBundle<HelloWorldConfiguration>() {
            @Override
            public Map<String, Map<String, String>> getViewConfiguration(HelloWorldConfiguration configuration) {
                return configuration.getViewRendererConfiguration();
            }
        });
    }

    @Override
    public void run(HelloWorldConfiguration configuration, Environment environment) {
        final PersonDAO dao = new PersonDAO(hibernateBundle.getSessionFactory());
        final Template template = configuration.buildTemplate();

        environment.healthChecks().register("template", new TemplateHealthCheck(template));
        environment.admin().addTask(new EchoTask());
        environment.getApplicationContext().addFilter(
                StructuredArgumentLoggingFilter.class,
                "/*",
                EnumSet.of(DispatcherType.REQUEST));

        environment.jersey().register(CorrelationIdFilter.class);
        environment.jersey().register(ResourceInfoLoggingFilter.class);
        environment.jersey().register(StatusInfoStructuredLoggingFilter.class);
        environment.jersey().register(DurationStructuredLoggingFilter.class);

        environment.jersey().register(DateRequiredFeature.class);
        environment.jersey().register(new AuthDynamicFeature(new BasicCredentialAuthFilter.Builder<User>()
                .setAuthenticator(new ExampleAuthenticator())
                .setAuthorizer(new ExampleAuthorizer())
                .setRealm("SUPER SECRET STUFF")
                .buildAuthFilter()));
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));
        environment.jersey().register(RolesAllowedDynamicFeature.class);
        environment.jersey().register(new HelloWorldResource(template));
        environment.jersey().register(new ViewResource());
        environment.jersey().register(new ProtectedResource());
        environment.jersey().register(new PeopleResource(dao));
        environment.jersey().register(new PersonResource(dao));
        environment.jersey().register(new FilteredResource());
    }
}
