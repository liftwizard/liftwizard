import java.util.Comparator;
import java.util.Optional;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

class Test
{
	Optional<String> streamMinWithMinOptional(MutableList<String> list)
	{
		return list.minOptional(Comparator.naturalOrder());
	}

	Optional<String> streamMaxWithMaxOptional(MutableList<String> list)
	{
		return list.maxOptional(Comparator.naturalOrder());
	}

	Optional<String> withCustomComparatorMin(MutableList<String> list)
	{
		return list.minOptional(Comparator.comparing(String::length));
	}

	Optional<String> withCustomComparatorMax(MutableList<String> list)
	{
		return list.maxOptional(Comparator.comparing(String::length));
	}

	Optional<Integer> withImmutableListMin(ImmutableList<Integer> list)
	{
		return list.minOptional(Comparator.naturalOrder());
	}

	Optional<Integer> withImmutableListMax(ImmutableList<Integer> list)
	{
		return list.maxOptional(Comparator.naturalOrder());
	}

	Optional<String> withMutableSetMin(MutableSet<String> set)
	{
		return set.minOptional(Comparator.naturalOrder());
	}

	Optional<String> withMutableSetMax(MutableSet<String> set)
	{
		return set.maxOptional(Comparator.naturalOrder());
	}

	void inIfCondition(MutableList<Integer> list)
	{
		Optional<Integer> min = list.minOptional(Comparator.naturalOrder());
		if (list.maxOptional(Comparator.naturalOrder()).isPresent())
		{
			this.doWork();
		}
	}

	void doWork()
	{
	}
}
