import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	MutableList<String> list;
	ArrayList<String> arrayList = new ArrayList<>();

	void test()
	{
		Map<String, Long> result1 = arrayList
			.stream()
			.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		Map<Integer, Long> result2 = list
			.stream()
			.collect(Collectors.groupingBy(String::length, Collectors.counting()));
		Map<String, List<String>> result3 = list
			.stream()
			.collect(Collectors.groupingBy(Function.identity(), Collectors.toList()));
		Map<String, List<String>> result4 = list.stream().collect(Collectors.groupingBy(Function.identity()));
	}
}
