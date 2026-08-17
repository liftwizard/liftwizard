import java.util.function.Predicate;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

class Test
{
	long streamFilterCount(MutableList<String> list, Predicate<String> predicate)
	{
		return list.count(predicate);
	}

	long withLambdaPredicate(MutableList<String> list)
	{
		return list
			.count((s) -> s.length() > 5);
	}

	long withMethodReferencePredicate(MutableList<String> list)
	{
		return list.count(String::isEmpty);
	}

	long withImmutableList(ImmutableList<String> list, Predicate<String> predicate)
	{
		return list.count(predicate);
	}

	long withMutableSet(MutableSet<Integer> set, Predicate<Integer> predicate)
	{
		return set.count(predicate);
	}

	void inIfCondition(MutableList<String> list)
	{
		if (list.count(String::isEmpty) > 5)
		{
			this.doWork();
		}
	}

	void doWork()
	{
	}
}
