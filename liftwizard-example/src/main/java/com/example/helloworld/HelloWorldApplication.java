package com.example.helloworld;

import java.util.Map;

import com.example.helloworld.auth.ExampleAuthenticator;
import com.example.helloworld.auth.ExampleAuthorizer;
import com.example.helloworld.cli.RenderCommand;
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
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import io.liftwizard.dropwizard.bundle.clock.ClockBundle;
import io.liftwizard.dropwizard.bundle.config.logging.ConfigLoggingBundle;
import io.liftwizard.dropwizard.bundle.environment.config.EnvironmentConfigBundle;
import io.liftwizard.dropwizard.bundle.h2.H2Bundle;
import io.liftwizard.dropwizard.bundle.httplogging.JerseyHttpLoggingBundle;
import io.liftwizard.dropwizard.bundle.objectmapper.ObjectMapperBundle;
import io.liftwizard.dropwizard.bundle.reladomo.ReladomoBundle;
import io.liftwizard.dropwizard.bundle.reladomo.connection.manager.ConnectionManagerBundle;
import io.liftwizard.dropwizard.bundle.reladomo.connection.manager.holder.ConnectionManagerHolderBundle;
import io.liftwizard.dropwizard.bundle.uuid.UUIDBundle;
import io.liftwizard.dropwizard.configuration.factory.JsonConfigurationFactoryFactory;
import io.liftwizard.servlet.logging.mdc.StructuredArgumentsMDCLogger;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

public class HelloWorldApplication
        extends Application<HelloWorldConfiguration>
{
    public static void main(String[] args)
            throws Exception
    {
        new HelloWorldApplication().run(args);
    }

    @Override
    public String getName()
    {
        return "hello-world";
    }

    @Override
    public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap)
    {
        bootstrap.setConfigurationFactoryFactory(new JsonConfigurationFactoryFactory<>());
        bootstrap.addBundle(new EnvironmentConfigBundle());

        bootstrap.addBundle(new ObjectMapperBundle());
        bootstrap.addBundle(new ConfigLoggingBundle());

        StructuredArgumentsMDCLogger structuredLogger = new StructuredArgumentsMDCLogger(bootstrap.getObjectMapper());
        bootstrap.addBundle(new JerseyHttpLoggingBundle(structuredLogger));

        bootstrap.addBundle(new ClockBundle());
        bootstrap.addBundle(new UUIDBundle());

        bootstrap.addBundle(new H2Bundle());
        bootstrap.addBundle(new ConnectionManagerBundle());
        bootstrap.addBundle(new ConnectionManagerHolderBundle());
        bootstrap.addBundle(new ReladomoBundle());

        bootstrap.addCommand(new RenderCommand());
        bootstrap.addBundle(new AssetsBundle());
        bootstrap.addBundle(new MigrationsBundle<HelloWorldConfiguration>()
        {
            @Override
            public DataSourceFactory getDataSourceFactory(HelloWorldConfiguration configuration)
            {
                return configuration
                        .getNamedDataSourcesFactory()
                        .getNamedDataSourceFactories()
                        .stream()
                        .filter(namedDataSourceFactory -> namedDataSourceFactory.getName().equals("liquibase"))
                        .findFirst()
                        .get();
            }
        });
        bootstrap.addBundle(new ViewBundle<HelloWorldConfiguration>()
        {
            @Override
            public Map<String, Map<String, String>> getViewConfiguration(HelloWorldConfiguration configuration)
            {
                return configuration.getViewRendererConfiguration();
            }
        });
    }

    @Override
    public void run(HelloWorldConfiguration configuration, Environment environment)
    {
        final PersonDAO dao      = new PersonDAO();
        final Template  template = configuration.buildTemplate();

        environment.healthChecks().register("template", new TemplateHealthCheck(template));
        environment.admin().addTask(new EchoTask());

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
