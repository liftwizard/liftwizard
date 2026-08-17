import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

class Test
{
	MutableList<String> mutableList;
	ImmutableList<String> immutableList;
	MutableSet<String> set;

	void test()
	{
		Map<Integer, Long> result1 = mutableList
			.stream()
			.collect(Collectors.groupingBy(String::length, Collectors.counting()));
		Map<String, Long> result2 = mutableList
			.stream()
			.collect(Collectors.groupingBy((s) -> s.substring(0, 1), Collectors.counting()));
		Map<Integer, Long> result3 = immutableList
			.stream()
			.collect(Collectors.groupingBy(String::length, Collectors.counting()));
		Map<Integer, Long> result4 = set.stream().collect(Collectors.groupingBy(String::length, Collectors.counting()));
	}
}
