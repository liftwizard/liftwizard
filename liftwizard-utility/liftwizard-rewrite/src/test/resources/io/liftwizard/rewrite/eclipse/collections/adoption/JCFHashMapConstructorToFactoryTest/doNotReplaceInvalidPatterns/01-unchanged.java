import java.util.HashMap;
import java.util.Map;

class Test
{
	private final HashMap<String, String> fieldConcreteType = new HashMap<>();

	void test(Map<String, String> inputMap)
	{
		HashMap<String, Integer> diamondMap = new HashMap<>();
		HashMap rawMap = new HashMap();
		HashMap<String, Integer> withInitialCapacity = new HashMap<>(10);
		HashMap<String, String> concreteFromMap = new HashMap<>(inputMap);
	}
}
