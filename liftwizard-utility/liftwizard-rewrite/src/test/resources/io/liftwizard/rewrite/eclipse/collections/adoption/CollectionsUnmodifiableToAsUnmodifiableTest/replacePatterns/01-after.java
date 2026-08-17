import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.factory.SortedSets;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;

class Test
{
	private final MutableList<String> fieldList = Lists.mutable.with("a", "b");
	private final List<String> fieldUnmodifiableList = fieldList.asUnmodifiable();
	private final Set<String> fieldUnmodifiableSet = Sets.mutable.with("x", "y").asUnmodifiable();
	private final Map<String, Integer> fieldUnmodifiableMap = Maps.mutable.with("key", 1).asUnmodifiable();
	private final SortedSet<String> fieldUnmodifiableSortedSet = SortedSets.mutable.with("x", "y").asUnmodifiable();
	private final SortedMap<String, Integer> fieldUnmodifiableSortedMap = SortedMaps.mutable.with("key", 1).asUnmodifiable();
	private final Collection<String> fieldUnmodifiableCollection = Lists.mutable.with("z").asUnmodifiable();

	void test()
	{
		MutableList<String> list = Lists.mutable.with("a", "b");
		List<String> unmodifiableList = list.asUnmodifiable();

		MutableSet<String> set = Sets.mutable.with("x", "y");
		Set<String> unmodifiableSet = set.asUnmodifiable();

		MutableMap<String, Integer> map = Maps.mutable.with("key", 1);
		Map<String, Integer> unmodifiableMap = map.asUnmodifiable();

		MutableSortedMap<String, Integer> sortedMap = SortedMaps.mutable.with("key", 1);
		SortedMap<String, Integer> unmodifiableSortedMap = sortedMap.asUnmodifiable();

		MutableSortedSet<String> sortedSet = SortedSets.mutable.with("x", "y");
		SortedSet<String> unmodifiableSortedSet = sortedSet.asUnmodifiable();

		Collection<String> unmodifiableCollection = list.asUnmodifiable();

		List<String> inlineExpression = Lists.mutable.with("c", "d").asUnmodifiable();

		MutableMap<String, Integer> anotherMap = Maps.mutable.with("key", 1);
		Map<String, Integer> result = anotherMap.asUnmodifiable();

		MutableList<String> list2 = Lists.mutable.with("e", "f");
		List<String> unmodifiableList1 = list2.asUnmodifiable();
		List<String> unmodifiableList2 = Lists.mutable.with("g").asUnmodifiable();

		MutableSet<String> set2 = Sets.mutable.with("z");
		Set<String> unmodifiableSet2 = set2.asUnmodifiable();

		List<String> chainResult = Lists.mutable.with("h", "i", "j").asUnmodifiable();
		int size = chainResult.size();
	}

	void testExplicitGenerics(Object element, Object key, Object value)
	{
		MutableList<String> list = Lists.mutable.with((String) element);
		List<String> unmodifiableList = list.asUnmodifiable();

		MutableSet<String> set = Sets.mutable.with((String) element);
		Set<String> unmodifiableSet = set.asUnmodifiable();

		MutableMap<String, Integer> map = Maps.mutable.with((String) key, (Integer) value);
		Map<String, Integer> unmodifiableMap = map.asUnmodifiable();

		MutableSortedMap<String, Integer> sortedMap = SortedMaps.mutable.with((String) key, (Integer) value);
		SortedMap<String, Integer> unmodifiableSortedMap = sortedMap.asUnmodifiable();

		MutableSortedSet<String> sortedSet = SortedSets.mutable.with((String) element);
		SortedSet<String> unmodifiableSortedSet = sortedSet.asUnmodifiable();

		Collection<String> unmodifiableCollection = list.asUnmodifiable();
	}

	List<String> getList()
	{
		MutableList<String> list = Lists.mutable.with("a", "b");
		return list.asUnmodifiable();
	}
}
