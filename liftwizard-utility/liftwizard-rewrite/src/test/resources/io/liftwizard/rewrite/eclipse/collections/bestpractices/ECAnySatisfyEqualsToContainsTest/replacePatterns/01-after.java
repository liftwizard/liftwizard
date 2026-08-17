import org.eclipse.collections.api.list.MutableList;

class Test
{
	private final String expected = "hello";

	void targetMethodReference(MutableList<String> list, String target)
	{
		boolean containsTarget = list.contains(target);

		if (list.contains(target))
		{
			this.doWork();
		}
	}

	void fieldReference(MutableList<String> list)
	{
		boolean containsExpected = list.contains(this.expected);
	}

	void integerType(MutableList<Integer> list, Integer target)
	{
		boolean containsTarget = list.contains(target);
	}

	void stringLiteralMethodReference(MutableList<String> list)
	{
		assertFalse(list.contains("Monkey"));
	}

	void doWork()
	{
	}

	void assertFalse(boolean value)
	{
	}
}
