import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;
import org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap;

class Test<T extends Comparable<T>>
{
	private final MutableSortedMap<String, String> fieldInterfaceEmpty = new TreeSortedMap<>();
	private final MutableSortedMap<String, String> fieldInterfaceComparator = new TreeSortedMap<>(
		Comparator.naturalOrder()
	);
	private final SortedMap<String, String> regularSortedMap = new TreeMap<>();
	private final MutableSortedMap<String, String> fieldInterfaceSortedMap = new TreeSortedMap<>(regularSortedMap);

	void test()
	{
		SortedMap<String, Integer> localSortedMap = new TreeMap<>();
		MutableSortedMap<String, Integer> diamondMap = new TreeSortedMap<>();
		MutableSortedMap<String, List<Integer>> nestedGenerics = new TreeSortedMap<>();
		MutableSortedMap<String, Integer> explicitSimple = new TreeSortedMap<String, Integer>();
		MutableSortedMap<String, List<Integer>> explicitNested = new TreeSortedMap<String, List<Integer>>();
		MutableSortedMap<String, MutableSortedMap<T, Integer>> nestedTypeParam = new TreeSortedMap<
			String,
			MutableSortedMap<T, Integer>
		>();
		org.eclipse.collections.api.map.sorted.MutableSortedMap<String, Integer> fullyQualified =
			new org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap<>();
		MutableSortedMap<String, Integer> withComparator = new TreeSortedMap<>(Comparator.naturalOrder());
		MutableSortedMap<String, Integer> withSortedMap = new TreeSortedMap<>(localSortedMap);
	}
}

class A<K extends Comparable<K>, V>
{
	@Override
	public MutableSortedMap<K, V> newEmpty()
	{
		return new TreeSortedMap<>();
	}
}
