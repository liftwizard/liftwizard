import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

class Test {
	private final TreeMap<String, String> fieldConcreteType = new TreeMap<>();

	void test(SortedMap<String, String> inputMap) {
		TreeMap<String, Integer> diamondMap = new TreeMap<>();
		TreeMap rawMap = new TreeMap();
		TreeMap<String, Integer> concreteTypeWithComparator = new TreeMap<>(Comparator.naturalOrder());
		TreeMap<String, String> concreteFromMap = new TreeMap<>(inputMap);
	}
}
