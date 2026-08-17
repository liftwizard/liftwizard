import java.util.List;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

class Test
{
	void streamForEachWithForEach(MutableList<String> list)
	{
		list.stream().forEach(System.out::println);
	}

	void streamForEachWithLambda(MutableList<String> list, List<String> target)
	{
		list.stream().forEach((s) -> target.add(s));
	}

	void streamForEachWithImmutableList(ImmutableList<String> list)
	{
		list.stream().forEach(System.out::println);
	}

	void streamForEachWithMutableSet(MutableSet<Integer> set, List<Integer> target)
	{
		set.stream().forEach((i) -> target.add(i * 2));
	}
}
