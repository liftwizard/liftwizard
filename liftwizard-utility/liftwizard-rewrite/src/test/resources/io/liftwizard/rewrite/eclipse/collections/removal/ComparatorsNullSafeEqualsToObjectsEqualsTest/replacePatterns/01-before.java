import org.eclipse.collections.impl.block.factory.Comparators;

class Test
{
	void test()
	{
		String left = "foo";
		String right = "bar";
		boolean equal = Comparators.nullSafeEquals(left, right);
		boolean notEqual = !Comparators.nullSafeEquals(left, right);
	}
}
