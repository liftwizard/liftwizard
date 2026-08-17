import java.util.List;
import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;
import org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap;

class Test
{
	private final MutableSortedMap<String, Integer> fieldSortedMap = SortedMaps.mutable.empty();

	void test()
	{
		MutableSortedMap<String, Integer> sortedMap = SortedMaps.mutable.empty();
		MutableSortedMap<String, Integer> fullyQualified = SortedMaps.mutable.empty();
		MutableSortedMap rawSortedMap = SortedMaps.mutable.empty();
		MutableSortedMap rawSortedMapFullyQualified = SortedMaps.mutable.empty();
		MutableSortedMap<String, List<Integer>> nestedGenerics = SortedMaps.mutable.empty();
		MutableSortedMap<String, Integer> treeSortedMap = TreeSortedMap.newMap();
		MutableSortedMap<String, Integer> map1 = SortedMaps.mutable.empty(),
			map2 = SortedMaps.mutable.with("a", 1);
	}
}
