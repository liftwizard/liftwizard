import org.eclipse.collections.api.map.MutableMap;

class Test
{
	void test(MutableMap<String, String> map, MutableMap<String, Integer> intMap, String key)
	{
		String result1 = map.getOrDefault("key", "default");
		String result2 = map.getOrDefault(key, "fallback");
		Integer result3 = intMap.getOrDefault("key", 0);
		String result4 = map.getOrDefault("key", "default");
	}
}
