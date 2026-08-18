/*~~>*/import java.util.Optional;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

class Test
{
	private static final Logger LOGGER = Logger.getLogger(Test.class);

	void fatalStringLiteral()
	{
		LOGGER.fatal("Simple message");
	}

	void fatalStringVariable(String message)
	{
		LOGGER.fatal(message);
	}

	void fatalObjectArgument(Object myObject)
	{
		LOGGER.fatal(myObject);
	}

	void fatalThrowableArgument(Exception exception)
	{
		LOGGER.fatal("Failure", exception);
	}

	void fatalMethodReference(Optional<Object> value)
	{
		value.ifPresent(LOGGER::fatal);
	}

	void bareLevelFatalConstant()
	{
		Level level = /*~~>*/Level.FATAL;
	}

	void logAtFatalLevel()
	{
		LOGGER.log(/*~~>*/Level.FATAL, "boom");
	}
}
