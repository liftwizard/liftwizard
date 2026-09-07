import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet;

class FactoryExamples
{
	void factories()
	{
		List<String> list = new ArrayList<>();
		List<String> listCapacity = new ArrayList<>(10);
		List<String> listCopy = new ArrayList<>(list);
		Map<String, Integer> map = new HashMap<>();
		Map<String, Integer> mapCapacity = new HashMap<>(10);
		Map<String, Integer> mapCopy = new HashMap<>(map);
		Set<String> set = new HashSet<>();
		Set<String> setCapacity = new HashSet<>(10);
		Set<String> setCopy = new HashSet<>(set);
		SortedMap<String, Integer> sortedMap = new TreeMap<>();
		SortedMap<String, Integer> sortedMapComparator = new TreeMap<>(Comparator.naturalOrder());
		SortedMap<String, Integer> sortedMapCopy = new TreeMap<>(sortedMap);
		SortedSet<String> sortedSet = new TreeSet<>();
		SortedSet<String> sortedSetComparator = new TreeSet<>(Comparator.naturalOrder());
		SortedSet<String> sortedSetCopy = new TreeSet<>(sortedSet);
		MutableList<String> ecList = new FastList<>();
		MutableList<String> ecListCapacity = new FastList<>(10);
		MutableList<String> ecListCopy = new FastList<>(ecList);
		MutableMap<String, Integer> ecMap = new UnifiedMap<>();
		MutableMap<String, Integer> ecMapCapacity = new UnifiedMap<>(10);
		MutableMap<String, Integer> ecMapCopy = new UnifiedMap<>(ecMap);
		MutableSet<String> ecSet = new UnifiedSet<>();
		MutableSet<String> ecSetCapacity = new UnifiedSet<>(10);
		MutableSet<String> ecSetCopy = new UnifiedSet<>(ecSet);
		MutableSortedMap<String, Integer> ecSortedMap = new TreeSortedMap<>();
		MutableSortedMap<String, Integer> ecSortedMapComparator = new TreeSortedMap<>(Comparator.naturalOrder());
		MutableSortedMap<String, Integer> ecSortedMapCopy = new TreeSortedMap<>(ecSortedMap);
		MutableSortedSet<String> ecSortedSet = new TreeSortedSet<>();
		MutableSortedSet<String> ecSortedSetComparator = new TreeSortedSet<>(Comparator.naturalOrder());
		MutableSortedSet<String> ecSortedSetCopy = new TreeSortedSet<>(ecSortedSet);
		MutableSortedSet<String> ecSortedSetComparatorCopy = new TreeSortedSet<>(
			Comparator.naturalOrder(),
			ecSortedSet
		);
		List<String> emptyList = Collections.emptyList();
		Set<String> emptySet = Collections.emptySet();
		Map<String, Integer> emptyMap = Collections.emptyMap();
		SortedSet<String> emptySortedSet = Collections.emptySortedSet();
		SortedMap<String, Integer> emptySortedMap = Collections.emptySortedMap();
	}
}
