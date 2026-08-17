import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Test
{
	private static final Logger LOGGER = LoggerFactory.getLogger(Test.class);

	void test(Object obj)
	{
		LOGGER.info("Value: " + obj.toString());
	}
}
