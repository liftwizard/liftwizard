public class Math
{
	public static final int TWO = 2;

	public static int addTwo(int a)
	{
		return a + TWO;
	}

	private Math() {
		throw new AssertionError("Suppress default constructor for noninstantiability");
	}
}
