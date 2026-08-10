import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.eclipse.collections.api.list.MutableList;

class Test {
	ArrayList<String> arrayList = new ArrayList<>();
	MutableList<String> list;

	void test() {
		Map<Integer, Long> result1 = arrayList.stream().collect(Collectors.groupingBy(String::length, Collectors.counting()));
		Map<Integer, List<String>> result2 = list.stream().collect(Collectors.groupingBy(String::length, Collectors.toList()));
		Map<Integer, List<String>> result3 = list.stream().collect(Collectors.groupingBy(String::length));
		Map<Integer, Long> result4 = list.stream().collect(Collectors.groupingBy(String::length, TreeMap::new, Collectors.counting()));
	}
}
