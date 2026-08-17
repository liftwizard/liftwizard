import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Test
{
	private static final Logger LOGGER = LoggerFactory.getLogger(Test.class);

	void stringConcatenationToParameterized(String name)
	{
		LOGGER.info("Hello " + name);
	}

	void multipleVariablesConcatenated(String first, String last)
	{
		LOGGER.info("User " + first + " " + last + " logged in");
	}

	void concatenationWithThrowableAsLastArg(String name, Exception e)
	{
		LOGGER.info("Error for " + name, e);
	}
}
