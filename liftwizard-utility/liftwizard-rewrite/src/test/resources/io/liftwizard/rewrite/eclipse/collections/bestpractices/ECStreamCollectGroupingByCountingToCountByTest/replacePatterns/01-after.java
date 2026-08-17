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
			.countBy(String::length);
		Map<String, Long> result2 = mutableList
			.countBy((s) -> s.substring(0, 1));
		Map<Integer, Long> result3 = immutableList
			.countBy(String::length);
		Map<Integer, Long> result4 = set.countBy(String::length);
	}
}
