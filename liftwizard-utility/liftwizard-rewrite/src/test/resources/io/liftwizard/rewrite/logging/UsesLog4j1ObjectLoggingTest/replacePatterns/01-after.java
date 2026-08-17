import java.util.function.Consumer;
import org.apache.log4j.Logger;

class MyEvent
{
	String name;
}

class Test
{
	private static final Logger LOGGER = Logger.getLogger(Test.class);

	void detectsObjectArgument(Object myObject)
	{
		/*~~>*/LOGGER.info(myObject);
	}

	void detectsCustomTypeArgument(MyEvent event)
	{
		/*~~>*/LOGGER.info(event);
	}

	void detectsAcrossLogLevels(Object obj)
	{
		/*~~>*/LOGGER.trace(obj);
		/*~~>*/LOGGER.debug(obj);
		/*~~>*/LOGGER.warn(obj);
		/*~~>*/LOGGER.error(obj);
		/*~~>*/LOGGER.fatal(obj);
	}

	void detectsObjectMethodReference()
	{
		take(/*~~>*/LOGGER::trace);
		take(/*~~>*/LOGGER::debug);
		take(/*~~>*/LOGGER::info);
		take(/*~~>*/LOGGER::warn);
		take(/*~~>*/LOGGER::error);
		take(/*~~>*/LOGGER::fatal);
	}

	void take(Consumer<MyEvent> sink)
	{
	}
}
