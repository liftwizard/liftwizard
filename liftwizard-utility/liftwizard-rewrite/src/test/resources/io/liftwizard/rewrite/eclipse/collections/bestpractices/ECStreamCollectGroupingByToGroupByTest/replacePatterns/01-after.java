import java.util.List;
import java.util.Map;
import java.util.Set;
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
		Map<Integer, Set<String>> result1 = mutableList
			.groupBy(String::length);
		Map<Integer, List<String>> result2 = mutableList
			.groupBy(String::length);
		Map<Integer, List<String>> result3 = mutableList.groupBy(String::length);
		Map<String, Set<String>> result4 = mutableList
			.groupBy((s) -> s.substring(0, 1));
		Map<Integer, Set<String>> result5 = immutableList
			.groupBy(String::length);
		Map<Integer, Set<String>> result6 = mutableSet
			.groupBy(String::length);
	}
}
