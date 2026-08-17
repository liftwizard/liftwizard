import java.util.Objects;

class Test
{
	void test(String left, String right, Integer a, Integer b)
	{
		boolean notEqualsPattern1 = !Objects.equals(left, right);
		boolean notEqualsPattern2 = !Objects.equals(right, left);
		boolean equalsPattern1 = Objects.equals(left, right);
		boolean equalsPattern2 = Objects.equals(right, left);
		boolean equalsPattern3 = Objects.equals(left, right);
		boolean equalsPattern4 = Objects.equals(left, right);
		boolean equalsPattern5 = Objects.equals(left, right);
		boolean equalsPattern6 = Objects.equals(left, right);
		boolean differentTypes = Objects.equals(a, b);
	}
}
