import java.util.function.Consumer;
import org.apache.log4j.Logger;

class Test
{
	private static final Logger LOGGER = Logger.getLogger(Test.class);

	void doesNotDetectStringLiteral()
	{
		LOGGER.info("Simple message");
	}

	void doesNotDetectStringVariable(String message)
	{
		LOGGER.info(message);
	}

	void doesNotDetectStringConcatenation(String name)
	{
		LOGGER.info("Hello " + name);
	}

	void doesNotDetectThrowables(Exception exception)
	{
		LOGGER.error(exception);
		LOGGER.error(exception.getClass().getName(), exception);
	}

	void doesNotDetectStringBuilder(StringBuilder builder)
	{
		LOGGER.info(builder);
	}

	void doesNotDetectStringMethodReference()
	{
		take(LOGGER::trace);
		take(LOGGER::debug);
		take(LOGGER::info);
		take(LOGGER::warn);
		take(LOGGER::error);
		take(LOGGER::fatal);
	}

	void take(Consumer<String> sink)
	{
	}
}
