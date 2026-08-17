public class PackagePrivateCtor
{
	private PackagePrivateCtor()
	{
		throw new AssertionError("Suppress default constructor for noninstantiability");
	}

	public static void utility()
	{
	}
}
