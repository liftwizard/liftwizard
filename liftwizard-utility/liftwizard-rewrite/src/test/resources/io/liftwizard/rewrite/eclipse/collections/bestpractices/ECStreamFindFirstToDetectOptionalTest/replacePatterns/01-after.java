import java.util.Optional;
import java.util.function.Predicate;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

class Test
{
	Optional<String> streamFilterFindFirst(MutableList<String> list, Predicate<String> predicate)
	{
		return list.detectOptional(predicate);
	}

	Optional<String> withLambdaPredicate(MutableList<String> list)
	{
		return list
			.detectOptional((s) -> s.length() > 5);
	}

	Optional<String> withMethodReferencePredicate(MutableList<String> list)
	{
		return list.detectOptional(String::isEmpty);
	}

	Optional<String> withImmutableList(ImmutableList<String> list, Predicate<String> predicate)
	{
		return list.detectOptional(predicate);
	}

	Optional<Integer> withMutableSet(MutableSet<Integer> set, Predicate<Integer> predicate)
	{
		return set.detectOptional(predicate);
	}

	void inIfPresent(MutableList<String> list)
	{
		list.detectOptional(String::isEmpty).ifPresent(System.out::println);
	}

	String withOrElse(MutableList<String> list)
	{
		return list
			.detectOptional((s) -> s.length() > 5)
			.orElse("default");
	}
}
