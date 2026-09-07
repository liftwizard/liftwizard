import java.util.Comparator;
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

class FactoryExamples
{
	void factories()
	{
		List<String> list = Lists.mutable.empty();
		List<String> listCapacity = Lists.mutable.withInitialCapacity(10);
		List<String> listCopy = Lists.mutable.withAll(list);
		Map<String, Integer> map = Maps.mutable.empty();
		Map<String, Integer> mapCapacity = Maps.mutable.withInitialCapacity(10);
		Map<String, Integer> mapCopy = Maps.mutable.withMap(map);
		Set<String> set = Sets.mutable.empty();
		Set<String> setCapacity = Sets.mutable.withInitialCapacity(10);
		Set<String> setCopy = Sets.mutable.withAll(set);
		SortedMap<String, Integer> sortedMap = SortedMaps.mutable.empty();
		SortedMap<String, Integer> sortedMapComparator = SortedMaps.mutable.with(Comparator.naturalOrder());
		SortedMap<String, Integer> sortedMapCopy = SortedMaps.mutable.withSortedMap(sortedMap);
		SortedSet<String> sortedSet = SortedSets.mutable.empty();
		SortedSet<String> sortedSetComparator = SortedSets.mutable.with(Comparator.naturalOrder());
		SortedSet<String> sortedSetCopy = SortedSets.mutable.withAll(sortedSet);
		MutableList<String> ecList = Lists.mutable.empty();
		MutableList<String> ecListCapacity = Lists.mutable.withInitialCapacity(10);
		MutableList<String> ecListCopy = Lists.mutable.withAll(ecList);
		MutableMap<String, Integer> ecMap = Maps.mutable.empty();
		MutableMap<String, Integer> ecMapCapacity = Maps.mutable.withInitialCapacity(10);
		MutableMap<String, Integer> ecMapCopy = Maps.mutable.withMap(ecMap);
		MutableSet<String> ecSet = Sets.mutable.empty();
		MutableSet<String> ecSetCapacity = Sets.mutable.withInitialCapacity(10);
		MutableSet<String> ecSetCopy = Sets.mutable.withAll(ecSet);
		MutableSortedMap<String, Integer> ecSortedMap = SortedMaps.mutable.empty();
		MutableSortedMap<String, Integer> ecSortedMapComparator = SortedMaps.mutable.with(Comparator.naturalOrder());
		MutableSortedMap<String, Integer> ecSortedMapCopy = SortedMaps.mutable.withSortedMap(ecSortedMap);
		MutableSortedSet<String> ecSortedSet = SortedSets.mutable.empty();
		MutableSortedSet<String> ecSortedSetComparator = SortedSets.mutable.with(Comparator.naturalOrder());
		MutableSortedSet<String> ecSortedSetCopy = SortedSets.mutable.withAll(ecSortedSet);
		MutableSortedSet<String> ecSortedSetComparatorCopy = SortedSets.mutable.withAll(Comparator.naturalOrder(), ecSortedSet);
		List<String> emptyList = Lists.fixedSize.empty();
		Set<String> emptySet = Sets.fixedSize.empty();
		Map<String, Integer> emptyMap = Maps.fixedSize.empty();
		SortedSet<String> emptySortedSet = SortedSets.mutable.empty();
		SortedMap<String, Integer> emptySortedMap = SortedMaps.mutable.empty();
	}
}
