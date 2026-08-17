import io.dropwizard.jersey.params.BooleanParam;

class Test
{
	void parameterUsage(BooleanParam flag)
	{
		Boolean value = flag.get();
	}

	void localVariable()
	{
		BooleanParam param = new BooleanParam("true");
		boolean result = param.get();
	}

	Boolean returnValue(BooleanParam param)
	{
		return param.get();
	}

	void passToMethod(BooleanParam param)
	{
		String s = String.valueOf(param.get());
	}
}
