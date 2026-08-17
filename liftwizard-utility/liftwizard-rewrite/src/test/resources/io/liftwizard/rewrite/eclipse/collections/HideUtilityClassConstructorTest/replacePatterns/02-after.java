public class PublicCtor
{
	private PublicCtor()
	{
		throw new AssertionError("Suppress default constructor for noninstantiability");
	}

	public static void utility()
	{
	}
}
