import java.util.function.BiConsumer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

class Test
{
	private static final Logger LOGGER = Logger.getLogger(Test.class);

	void staticStandardLevel()
	{
		LOGGER.log(Level.INFO, "hello");
	}

	void objectMessage(Object myObject)
	{
		LOGGER.log(Level.INFO, myObject);
	}

	void dynamicPriority(Priority priority, String message)
	{
		LOGGER.log(priority, message);
	}

	void withThrowable(Exception exception)
	{
		LOGGER.log(Level.ERROR, "boom", exception);
	}

	void methodReference()
	{
		BiConsumer<Priority, Object> ref = LOGGER::log;
	}
}
