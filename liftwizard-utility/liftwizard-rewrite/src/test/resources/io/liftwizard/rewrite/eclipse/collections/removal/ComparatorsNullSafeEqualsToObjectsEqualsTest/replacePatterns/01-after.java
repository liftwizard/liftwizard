import java.util.Objects;

class Test
{
	void test()
	{
		String left = "foo";
		String right = "bar";
		boolean equal = Objects.equals(left, right);
		boolean notEqual = !Objects.equals(left, right);
	}
}
