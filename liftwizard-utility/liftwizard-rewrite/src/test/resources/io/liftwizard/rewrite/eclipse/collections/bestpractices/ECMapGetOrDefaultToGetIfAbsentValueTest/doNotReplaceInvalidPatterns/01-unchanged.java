import java.util.HashMap;
import java.util.Map;

class Test {
	void test(Map<String, String> map, HashMap<String, String> hashMap) {
		String result1 = map.getOrDefault("key", "default");
		String result2 = hashMap.getOrDefault("key", "default");
	}
}
