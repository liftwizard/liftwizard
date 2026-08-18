public class StaticFieldsOnly
{
	private StaticFieldsOnly()
	{
		throw new AssertionError("Suppress default constructor for noninstantiability");
	}

	public static int a;
}
