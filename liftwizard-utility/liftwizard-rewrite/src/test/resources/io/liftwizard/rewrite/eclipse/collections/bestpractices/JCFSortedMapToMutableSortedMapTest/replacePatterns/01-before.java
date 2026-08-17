import java.util.List;
import java.util.SortedMap;
import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap;

class Test
{
	private final SortedMap<String, Integer> fieldSortedMap = SortedMaps.mutable.empty();

	void test()
	{
		SortedMap<String, Integer> sortedMap = SortedMaps.mutable.empty();
		java.util.SortedMap<String, Integer> fullyQualified = SortedMaps.mutable.empty();
		SortedMap rawSortedMap = SortedMaps.mutable.empty();
		java.util.SortedMap rawSortedMapFullyQualified = SortedMaps.mutable.empty();
		SortedMap<String, List<Integer>> nestedGenerics = SortedMaps.mutable.empty();
		SortedMap<String, Integer> treeSortedMap = TreeSortedMap.newMap();
		SortedMap<String, Integer> map1 = SortedMaps.mutable.empty(),
			map2 = SortedMaps.mutable.with("a", 1);
	}
}
