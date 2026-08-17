import io.dropwizard.core.cli.Command;
import io.dropwizard.core.cli.ConfiguredCommand;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.logging.common.AbstractAppenderFactory;
import io.dropwizard.logging.common.filter.FilterFactory;
import io.dropwizard.logging.common.layout.LayoutFactory;
import io.dropwizard.metrics.common.ReporterFactory;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.views.common.View;

class MyApp
	extends Application<Configuration>
{
	public void run(String... args)
	{
	}

	void setupPackage(Bootstrap<?> bootstrap, Environment environment)
	{
	}

	void loggingPackage(FilterFactory<?> filterFactory, LayoutFactory<?> layoutFactory)
	{
	}

	void metricsPackage(ReporterFactory reporterFactory)
	{
	}
}

abstract class MyCommand
	extends Command
{
	public void run(Object environment, Object namespace)
	{
	}
}

class MyView
	extends View
{
	protected MyView()
	{
		super("my-template.ftl");
	}
}
