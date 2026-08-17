import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

class Test
{
	private final String expected = "hello";

	void streamAnyMatchWithContains(MutableList<String> list, String target)
	{
		boolean containsTarget = list.stream().anyMatch(target::equals);

		if (list.stream().anyMatch(target::equals))
		{
			this.doWork();
		}
	}

	void withFieldReference(MutableList<String> list)
	{
		boolean containsExpected = list.stream().anyMatch(this.expected::equals);
	}

	void withIntegerType(MutableList<Integer> list, Integer target)
	{
		boolean containsTarget = list.stream().anyMatch(target::equals);
	}

	void withImmutableList(ImmutableList<String> list, String target)
	{
		boolean containsTarget = list.stream().anyMatch(target::equals);
	}

	void withMutableSet(MutableSet<String> set, String target)
	{
		boolean containsTarget = set.stream().anyMatch(target::equals);
	}

	void doWork()
	{
	}
}
