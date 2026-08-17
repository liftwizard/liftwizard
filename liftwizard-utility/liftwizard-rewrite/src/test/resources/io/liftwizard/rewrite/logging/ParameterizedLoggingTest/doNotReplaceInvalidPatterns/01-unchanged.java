import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Test
{
	private static final Logger LOGGER = LoggerFactory.getLogger(Test.class);

	void doesNotTransformStringArgument()
	{
		LOGGER.info("Simple message");
	}

	void doesNotTransformAlreadyParameterized(String name)
	{
		LOGGER.info("Hello {}", name);
	}
}
