import org.apache.log4j.Logger;

class Test
{
	private static final Logger LOGGER = Logger.getLogger(Test.class);

	void test(Object myObject)
	{
		LOGGER.info(myObject);
	}
}
