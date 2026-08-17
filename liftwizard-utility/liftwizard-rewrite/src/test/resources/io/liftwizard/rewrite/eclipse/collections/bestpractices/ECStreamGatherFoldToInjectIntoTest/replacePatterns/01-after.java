import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

class Test
{
	Integer foldWithOrElseThrow(MutableList<Integer> list)
	{
		return list
			.injectInto(0, Integer::sum);
	}

	Integer foldWithGet(MutableList<Integer> list)
	{
		return list
			.injectInto(0, Integer::sum);
	}

	Integer foldWithLambda(MutableList<Integer> list)
	{
		return list
			.injectInto(0, (a, b) -> a + b);
	}

	Integer foldWithImmutableList(ImmutableList<Integer> list)
	{
		return list
			.injectInto(0, Integer::sum);
	}

	String foldWithStringConcat(MutableList<String> list)
	{
		return list
			.injectInto("", String::concat);
	}

	Integer foldWithMutableSet(MutableSet<Integer> set)
	{
		return set
			.injectInto(1, (a, b) -> a * b);
	}
}
