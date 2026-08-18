import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.eclipse.collections.api.factory.SortedMaps;

class Test
{
	private final SortedMap<String, String> fieldInterfaceEmpty = SortedMaps.mutable.empty();
	private final SortedMap<String, String> fieldInterfaceComparator = SortedMaps.mutable.with(Comparator.naturalOrder());
	private final SortedMap<String, String> fieldInterfaceMap = SortedMaps.mutable.withSortedMap(this.fieldInterfaceEmpty);

	void test(SortedMap<String, String> inputMap)
	{
		SortedMap<String, Integer> typeInference = SortedMaps.mutable.empty();
		SortedMap<String, List<Integer>> nestedGenerics = SortedMaps.mutable.empty();
		SortedMap<String, ? extends Number> wildcardGenerics = SortedMaps.mutable.empty();
		SortedMap<String, Integer> explicitSimple = SortedMaps.mutable.<String, Integer>empty();
		SortedMap<String, List<Integer>> explicitNested = SortedMaps.mutable.<String, List<Integer>>empty();
		java.util.SortedMap<String, Integer> fullyQualified = SortedMaps.mutable.empty();
		SortedMap<String, String> sortedMapWithComparator = SortedMaps.mutable.with(Comparator.naturalOrder());
		SortedMap<String, String> interfaceFromMap = SortedMaps.mutable.withSortedMap(inputMap);
		SortedMap<String, String> fromMap = SortedMaps.mutable.withSortedMap(this.fieldInterfaceEmpty);
	}
}
