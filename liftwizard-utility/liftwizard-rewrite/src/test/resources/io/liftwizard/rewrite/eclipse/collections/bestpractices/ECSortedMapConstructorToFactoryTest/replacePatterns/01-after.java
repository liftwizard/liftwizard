import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;

class Test<T extends Comparable<T>>
{
	private final MutableSortedMap<String, String> fieldInterfaceEmpty = SortedMaps.mutable.empty();
	private final MutableSortedMap<String, String> fieldInterfaceComparator = SortedMaps.mutable.with(Comparator.naturalOrder());
	private final SortedMap<String, String> regularSortedMap = new TreeMap<>();
	private final MutableSortedMap<String, String> fieldInterfaceSortedMap = SortedMaps.mutable.withSortedMap(regularSortedMap);

	void test()
	{
		SortedMap<String, Integer> localSortedMap = new TreeMap<>();
		MutableSortedMap<String, Integer> diamondMap = SortedMaps.mutable.empty();
		MutableSortedMap<String, List<Integer>> nestedGenerics = SortedMaps.mutable.empty();
		MutableSortedMap<String, Integer> explicitSimple = SortedMaps.mutable.<String, Integer>empty();
		MutableSortedMap<String, List<Integer>> explicitNested = SortedMaps.mutable.<String, List<Integer>>empty();
		MutableSortedMap<String, MutableSortedMap<T, Integer>> nestedTypeParam = SortedMaps.mutable.<String, MutableSortedMap<T, Integer>>empty();
		org.eclipse.collections.api.map.sorted.MutableSortedMap<String, Integer> fullyQualified =
				SortedMaps.mutable.empty();
		MutableSortedMap<String, Integer> withComparator = SortedMaps.mutable.with(Comparator.naturalOrder());
		MutableSortedMap<String, Integer> withSortedMap = SortedMaps.mutable.withSortedMap(localSortedMap);
	}
}

class A<K extends Comparable<K>, V>
{
	@Override
	public MutableSortedMap<K, V> newEmpty()
	{
		return SortedMaps.mutable.empty();
	}
}
