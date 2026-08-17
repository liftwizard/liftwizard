import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

class Test
{
	Integer withMethodReference(MutableList<Integer> list)
	{
		return list.injectInto(0, Integer::sum);
	}

	Integer withLambda(MutableList<Integer> list)
	{
		return list.injectInto(0, (a, b) -> a + b);
	}

	Integer withImmutableList(ImmutableList<Integer> list)
	{
		return list.injectInto(0, Integer::sum);
	}

	Integer withMutableSet(MutableSet<Integer> set)
	{
		return set.injectInto(1, (a, b) -> a * b);
	}

	String withStringConcat(MutableList<String> list)
	{
		return list.injectInto("", String::concat);
	}

	void inIfCondition(MutableList<Integer> list)
	{
		Integer sum = list.injectInto(0, Integer::sum);
		if (list.injectInto(0, Integer::sum) > 100)
		{
			this.doWork();
		}
	}

	void doWork()
	{
	}
}
