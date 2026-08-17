import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

class Test
{
	MutableList<String> mutableList;
	ImmutableList<String> immutableList;
	MutableSet<String> mutableSet;

	void test()
	{
		Map<String, Long> result1 = mutableList
			.stream()
			.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		Map<String, Long> result2 = immutableList
			.stream()
			.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		Map<String, Long> result3 = mutableSet
			.stream()
			.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
	}
}
