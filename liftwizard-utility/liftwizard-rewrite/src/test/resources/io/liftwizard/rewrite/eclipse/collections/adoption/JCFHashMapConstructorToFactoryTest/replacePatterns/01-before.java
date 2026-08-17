import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Test
{
	private final Map<String, String> fieldInterfaceEmpty = new HashMap<>();
	private final Map<String, Integer> fieldInterfaceCapacity = new HashMap<>(10);
	private final Map<String, String> fieldInterfaceMap = new HashMap<>(this.fieldInterfaceEmpty);

	void test(Map<String, String> inputMap)
	{
		Map<String, Integer> typeInference = new HashMap<>();
		Map<String, List<Integer>> nestedGenerics = new HashMap<>();
		Map<String, ? extends Number> wildcardGenerics = new HashMap<>();
		Map<String, Integer> explicitSimple = new HashMap<String, Integer>();
		Map<String, List<Integer>> explicitNested = new HashMap<String, List<Integer>>();
		java.util.Map<String, Integer> fullyQualified = new HashMap<>();
		Map<String, Integer> withCapacity20 = new HashMap<>(20);
		Map<String, Integer> explicit30 = new HashMap<String, Integer>(30);
		Map<String, String> interfaceFromMap = new HashMap<>(inputMap);
		Map<String, String> fromMap = new HashMap<>(this.fieldInterfaceEmpty);
	}
}
