import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

class Test
{
	private final SortedMap<String, String> fieldInterfaceEmpty = new TreeMap<>();
	private final SortedMap<String, String> fieldInterfaceComparator = new TreeMap<>(Comparator.naturalOrder());
	private final SortedMap<String, String> fieldInterfaceMap = new TreeMap<>(this.fieldInterfaceEmpty);

	void test(SortedMap<String, String> inputMap)
	{
		SortedMap<String, Integer> typeInference = new TreeMap<>();
		SortedMap<String, List<Integer>> nestedGenerics = new TreeMap<>();
		SortedMap<String, ? extends Number> wildcardGenerics = new TreeMap<>();
		SortedMap<String, Integer> explicitSimple = new TreeMap<String, Integer>();
		SortedMap<String, List<Integer>> explicitNested = new TreeMap<String, List<Integer>>();
		java.util.SortedMap<String, Integer> fullyQualified = new TreeMap<>();
		SortedMap<String, String> sortedMapWithComparator = new TreeMap<>(Comparator.naturalOrder());
		SortedMap<String, String> interfaceFromMap = new TreeMap<>(inputMap);
		SortedMap<String, String> fromMap = new TreeMap<>(this.fieldInterfaceEmpty);
	}
}
