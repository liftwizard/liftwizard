import java.util.function.Predicate;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

class Test
{
	long streamFilterCount(MutableList<String> list, Predicate<String> predicate)
	{
		return list.stream().filter(predicate).count();
	}

	long withLambdaPredicate(MutableList<String> list)
	{
		return list
			.stream()
			.filter((s) -> s.length() > 5)
			.count();
	}

	long withMethodReferencePredicate(MutableList<String> list)
	{
		return list.stream().filter(String::isEmpty).count();
	}

	long withImmutableList(ImmutableList<String> list, Predicate<String> predicate)
	{
		return list.stream().filter(predicate).count();
	}

	long withMutableSet(MutableSet<Integer> set, Predicate<Integer> predicate)
	{
		return set.stream().filter(predicate).count();
	}

	void inIfCondition(MutableList<String> list)
	{
		if (list.stream().filter(String::isEmpty).count() > 5)
		{
			this.doWork();
		}
	}

	void doWork()
	{
	}
}
