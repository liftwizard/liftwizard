import java.util.stream.Gatherers;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

class Test
{
	Integer foldWithOrElseThrow(MutableList<Integer> list)
	{
		return list
			.stream()
			.gather(Gatherers.fold(() -> 0, Integer::sum))
			.findFirst()
			.orElseThrow();
	}

	Integer foldWithGet(MutableList<Integer> list)
	{
		return list
			.stream()
			.gather(Gatherers.fold(() -> 0, Integer::sum))
			.findFirst()
			.get();
	}

	Integer foldWithLambda(MutableList<Integer> list)
	{
		return list
			.stream()
			.gather(Gatherers.fold(() -> 0, (a, b) -> a + b))
			.findFirst()
			.orElseThrow();
	}

	Integer foldWithImmutableList(ImmutableList<Integer> list)
	{
		return list
			.stream()
			.gather(Gatherers.fold(() -> 0, Integer::sum))
			.findFirst()
			.orElseThrow();
	}

	String foldWithStringConcat(MutableList<String> list)
	{
		return list
			.stream()
			.gather(Gatherers.fold(() -> "", String::concat))
			.findFirst()
			.orElseThrow();
	}

	Integer foldWithMutableSet(MutableSet<Integer> set)
	{
		return set
			.stream()
			.gather(Gatherers.fold(() -> 1, (a, b) -> a * b))
			.findFirst()
			.orElseThrow();
	}
}
