class Test
{
	void parameterUsage(Boolean flag)
	{
		Boolean value = flag;
	}

	void localVariable()
	{
		Boolean param = new Boolean("true");
		boolean result = param;
	}

	Boolean returnValue(Boolean param)
	{
		return param;
	}

	void passToMethod(Boolean param)
	{
		String s = String.valueOf(param);
	}
}
