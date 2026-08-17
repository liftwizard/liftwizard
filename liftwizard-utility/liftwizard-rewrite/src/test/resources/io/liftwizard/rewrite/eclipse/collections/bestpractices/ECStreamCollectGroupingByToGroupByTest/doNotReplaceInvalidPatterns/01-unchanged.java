import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	MutableList<String> list;
	ArrayList<String> arrayList = new ArrayList<>();

	void test()
	{
		Map<Integer, Set<String>> result1 = arrayList
			.stream()
			.collect(Collectors.groupingBy(String::length, Collectors.toSet()));
		Map<Integer, Long> result2 = list
			.stream()
			.collect(Collectors.groupingBy(String::length, Collectors.counting()));
		Map<Integer, Set<String>> result3 = list
			.stream()
			.collect(Collectors.groupingBy(String::length, TreeMap::new, Collectors.toSet()));
	}
}
