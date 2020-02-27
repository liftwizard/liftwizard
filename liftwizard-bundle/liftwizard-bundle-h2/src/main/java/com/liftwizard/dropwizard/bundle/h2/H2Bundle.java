package com.liftwizard.dropwizard.bundle.h2;

import java.sql.SQLException;
import java.util.List;

import javax.annotation.Nonnull;
import javax.servlet.ServletRegistration.Dynamic;

import com.google.auto.service.AutoService;
import com.liftwizard.dropwizard.bundle.prioritized.PrioritizedBundle;
import com.liftwizard.dropwizard.configuration.h2.H2Factory;
import com.liftwizard.dropwizard.configuration.h2.H2FactoryProvider;
import io.dropwizard.setup.Environment;
import org.h2.server.web.WebServlet;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Based on H2ConfigurationHelper in the jhipster project.
 *
 * @see <a href="https://github.com/jhipster/jhipster/blob/master/jhipster-framework/src/main/java/io/github/jhipster/config/h2/H2ConfigurationHelper.java">H2ConfigurationHelper</a>
 */
@AutoService(PrioritizedBundle.class)
public class H2Bundle
        implements PrioritizedBundle<Object>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(H2Bundle.class);

    @Override
    public int getPriority()
    {
        return -7;
    }

    @Override
    public void runWithMdc(@Nonnull Object configuration, @Nonnull Environment environment)
    {
        H2FactoryProvider h2FactoryProvider = this.safeCastConfiguration(H2FactoryProvider.class, configuration);
        H2Factory         h2Factory         = h2FactoryProvider.getH2Factory();
        if (!h2Factory.isEnabled())
        {
            LOGGER.info("{} disabled.", H2Bundle.class.getSimpleName());
            return;
        }

        LOGGER.info("Running {}.", H2Bundle.class.getSimpleName());

        Server tcpServer = this.createTcpServer(h2Factory.getTcpServerArgs());
        environment.lifecycle().manage(new TcpServerShutdownHook(tcpServer));

        String servletName        = h2Factory.getServletName();
        String servletUrlMapping  = h2Factory.getServletUrlMapping();
        String propertiesLocation = h2Factory.getPropertiesLocation();

        Dynamic h2ConsoleServlet = environment.servlets().addServlet(servletName, new WebServlet());
        h2ConsoleServlet.addMapping(servletUrlMapping);
        h2ConsoleServlet.setInitParameter("-properties", propertiesLocation);
        h2ConsoleServlet.setLoadOnStartup(1);

        // TODO: Add logging about what's happening here

        LOGGER.info("Completing {}.", H2Bundle.class.getSimpleName());
    }

    @Nonnull
    private Server createTcpServer(List<String> tcpServerArgs)
    {
        LOGGER.info("Starting H2 TCP Server with args: {}", tcpServerArgs);
        try
        {
            Server server = Server.createTcpServer(tcpServerArgs.toArray(new String[]{}));
            server.start();
            LOGGER.info(server.getStatus());
            return server;
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }
}
