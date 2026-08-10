import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.collections.api.list.MutableList;

class Test {
	MutableList<String> list;
	ArrayList<String> arrayList = new ArrayList<>();

	void test() {
		Map<Integer, String> result1 = list.stream().collect(Collectors.toMap(String::length, String::toUpperCase));
		Map<Integer, String> result2 = arrayList.stream().collect(Collectors.toMap(String::length, Function.identity()));
		Map<Integer, String> result3 = list.stream().collect(Collectors.toMap(String::length, Function.identity(), (a, b) -> a));
	}
}
