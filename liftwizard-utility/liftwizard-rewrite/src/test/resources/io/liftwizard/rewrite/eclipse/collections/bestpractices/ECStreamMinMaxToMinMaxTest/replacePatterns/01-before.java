import java.util.Comparator;
import java.util.Optional;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

class Test
{
	Optional<String> streamMinWithMinOptional(MutableList<String> list)
	{
		return list.stream().min(Comparator.naturalOrder());
	}

	Optional<String> streamMaxWithMaxOptional(MutableList<String> list)
	{
		return list.stream().max(Comparator.naturalOrder());
	}

	Optional<String> withCustomComparatorMin(MutableList<String> list)
	{
		return list.stream().min(Comparator.comparing(String::length));
	}

	Optional<String> withCustomComparatorMax(MutableList<String> list)
	{
		return list.stream().max(Comparator.comparing(String::length));
	}

	Optional<Integer> withImmutableListMin(ImmutableList<Integer> list)
	{
		return list.stream().min(Comparator.naturalOrder());
	}

	Optional<Integer> withImmutableListMax(ImmutableList<Integer> list)
	{
		return list.stream().max(Comparator.naturalOrder());
	}

	Optional<String> withMutableSetMin(MutableSet<String> set)
	{
		return set.stream().min(Comparator.naturalOrder());
	}

	Optional<String> withMutableSetMax(MutableSet<String> set)
	{
		return set.stream().max(Comparator.naturalOrder());
	}

	void inIfCondition(MutableList<Integer> list)
	{
		Optional<Integer> min = list.stream().min(Comparator.naturalOrder());
		if (list.stream().max(Comparator.naturalOrder()).isPresent())
		{
			this.doWork();
		}
	}

	void doWork()
	{
	}
}
