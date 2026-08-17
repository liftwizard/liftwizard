import org.eclipse.collections.api.map.MutableMap;

class Test
{
	void test(MutableMap<String, String> map, MutableMap<String, Integer> intMap, String key)
	{
		String result1 = map.getIfAbsentValue("key", "default");
		String result2 = map.getIfAbsentValue(key, "fallback");
		Integer result3 = intMap.getIfAbsentValue("key", 0);
		String result4 = map.getIfAbsentValue("key", "default");
	}
}
