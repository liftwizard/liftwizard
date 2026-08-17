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
		Map<Integer, String> result1 = mutableList
			.groupByUniqueKey(String::length);
		Map<String, String> result2 = mutableList
			.groupByUniqueKey((s) -> s.substring(0, 1));
		Map<Integer, String> result3 = immutableList
			.groupByUniqueKey(String::length);
		Map<Integer, String> result4 = mutableSet
			.groupByUniqueKey(String::length);
	}
}
