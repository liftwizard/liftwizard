import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

class Test
{
	private final String expected = "hello";

	void streamAnyMatchWithContains(MutableList<String> list, String target)
	{
		boolean containsTarget = list.contains(target);

		if (list.contains(target))
		{
			this.doWork();
		}
	}

	void withFieldReference(MutableList<String> list)
	{
		boolean containsExpected = list.contains(this.expected);
	}

	void withIntegerType(MutableList<Integer> list, Integer target)
	{
		boolean containsTarget = list.contains(target);
	}

	void withImmutableList(ImmutableList<String> list, String target)
	{
		boolean containsTarget = list.contains(target);
	}

	void withMutableSet(MutableSet<String> set, String target)
	{
		boolean containsTarget = set.contains(target);
	}

	void doWork()
	{
	}
}
