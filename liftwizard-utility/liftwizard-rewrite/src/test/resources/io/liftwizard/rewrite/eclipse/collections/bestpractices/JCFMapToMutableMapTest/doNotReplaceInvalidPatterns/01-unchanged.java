import java.util.HashMap;
import java.util.Map;

class Test {
	Map<String, Integer> getMap() {
		return Map.of("a", 1);
	}

	void processMap(Map<String, Integer> map) {
		map.get("key");
	}

	void test() {
		Map<String, Integer> hashMap = new HashMap<>();
		Map<String, Integer> emptyMap = Map.of();
		Map<String, Integer> mapWithEntry = Map.of("a", 1);
	}
}
