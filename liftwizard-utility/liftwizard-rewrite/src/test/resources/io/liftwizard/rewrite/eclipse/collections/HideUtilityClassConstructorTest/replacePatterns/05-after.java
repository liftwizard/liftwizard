public class AlreadyPrivateNoAssertionError
{
	private AlreadyPrivateNoAssertionError()
	{
		throw new AssertionError("Suppress default constructor for noninstantiability");
	}

	public static String foo()
	{
		return "foo";
	}
}
