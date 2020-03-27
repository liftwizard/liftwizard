package com.liftwizard.dropwizard.configuration.connectionmanager;

import java.io.File;
import java.net.URL;
import java.util.TimeZone;

import javax.validation.Validator;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.gs.fw.common.mithra.connectionmanager.SourcelessConnectionManager;
import com.gs.fw.common.mithra.databasetype.GenericDatabaseType;
import io.dropwizard.configuration.JsonConfigurationFactory;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ConnectionManagerFactoryTest
{
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final Validator    validator    = Validators.newValidator();

    private final JsonConfigurationFactory<ConnectionManagerFactory> factory =
            new JsonConfigurationFactory<>(ConnectionManagerFactory.class, this.validator, this.objectMapper, "dw");

    @Test
    public void createSourcelessConnectionManager() throws Exception
    {
        URL                      resource                 = Resources.getResource("test-config.json");
        File                     json                     = new File(resource.toURI());
        ConnectionManagerFactory connectionManagerFactory = this.factory.build(json);

        DataSourceFactory dataSourceFactory = new DataSourceFactory();
        ManagedDataSource managedDataSource = dataSourceFactory.build(new MetricRegistry(), "test");

        SourcelessConnectionManager sourcelessConnectionManager =
                connectionManagerFactory.createSourcelessConnectionManager(managedDataSource);

        assertThat(sourcelessConnectionManager.getDatabaseIdentifier(), is("schemaName"));
        assertThat(sourcelessConnectionManager.getDatabaseTimeZone(), is(TimeZone.getTimeZone("America/New_York")));
        assertThat(sourcelessConnectionManager.getDatabaseType(), is(GenericDatabaseType.getInstance()));
    }
}
