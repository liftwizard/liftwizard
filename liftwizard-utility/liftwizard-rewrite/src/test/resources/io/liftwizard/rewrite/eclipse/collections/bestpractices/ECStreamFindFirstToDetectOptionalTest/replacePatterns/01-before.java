import java.util.Optional;
import java.util.function.Predicate;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

class Test
{
	Optional<String> streamFilterFindFirst(MutableList<String> list, Predicate<String> predicate)
	{
		return list.stream().filter(predicate).findFirst();
	}

	Optional<String> withLambdaPredicate(MutableList<String> list)
	{
		return list
			.stream()
			.filter((s) -> s.length() > 5)
			.findFirst();
	}

	Optional<String> withMethodReferencePredicate(MutableList<String> list)
	{
		return list.stream().filter(String::isEmpty).findFirst();
	}

	Optional<String> withImmutableList(ImmutableList<String> list, Predicate<String> predicate)
	{
		return list.stream().filter(predicate).findFirst();
	}

	Optional<Integer> withMutableSet(MutableSet<Integer> set, Predicate<Integer> predicate)
	{
		return set.stream().filter(predicate).findFirst();
	}

	void inIfPresent(MutableList<String> list)
	{
		list.stream().filter(String::isEmpty).findFirst().ifPresent(System.out::println);
	}

	String withOrElse(MutableList<String> list)
	{
		return list
			.stream()
			.filter((s) -> s.length() > 5)
			.findFirst()
			.orElse("default");
	}
}
