import org.eclipse.collections.api.list.MutableList;

class Test
{
	private final String expected = "hello";

	void targetMethodReference(MutableList<String> list, String target)
	{
		boolean containsTarget = list.anySatisfy(target::equals);

		if (list.anySatisfy(target::equals))
		{
			this.doWork();
		}
	}

	void fieldReference(MutableList<String> list)
	{
		boolean containsExpected = list.anySatisfy(this.expected::equals);
	}

	void integerType(MutableList<Integer> list, Integer target)
	{
		boolean containsTarget = list.anySatisfy(target::equals);
	}

	void stringLiteralMethodReference(MutableList<String> list)
	{
		assertFalse(list.anySatisfy("Monkey"::equals));
	}

	void doWork()
	{
	}

	void assertFalse(boolean value)
	{
	}
}
