import java.util.List;
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
		Map<Boolean, List<String>> result1 = mutableList
			.stream()
			.collect(Collectors.partitioningBy((s) -> s.length() > 3));
		Map<Boolean, List<String>> result2 = mutableList.stream().collect(Collectors.partitioningBy(String::isEmpty));
		Map<Boolean, List<String>> result3 = immutableList
			.stream()
			.collect(Collectors.partitioningBy((s) -> s.length() > 3));
		Map<Boolean, List<String>> result4 = set.stream().collect(Collectors.partitioningBy((s) -> s.length() > 3));
	}
}
