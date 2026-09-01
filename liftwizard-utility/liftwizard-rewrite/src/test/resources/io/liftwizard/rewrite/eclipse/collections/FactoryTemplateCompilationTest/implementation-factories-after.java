import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.factory.Sets;
import org.eclipse.collections.impl.factory.SortedMaps;
import org.eclipse.collections.impl.factory.SortedSets;

class FactoryExamples
{
	void factories()
	{
		List<String> list = Lists.adapt(List.of());
		Map<String, Integer> map = Maps.mutable.empty();
		Set<String> set = Sets.union(Set.of(), Set.of());
		SortedMap<String, Integer> sortedMap = SortedMaps.mutable.empty();
		SortedSet<String> sortedSet = SortedSets.mutable.empty();

		List<String> listCopy = Lists.mutable.withAll(list);
		Map<String, Integer> mapCopy = Maps.mutable.withMap(map);
		Set<String> setCopy = Sets.mutable.withAll(set);
		SortedMap<String, Integer> sortedMapCopy = SortedMaps.mutable.withSortedMap(sortedMap);
		SortedSet<String> sortedSetCopy = SortedSets.mutable.withAll(sortedSet);
		List<String> listCapacity = Lists.mutable.withInitialCapacity(10);
		Map<String, Integer> mapCapacity = Maps.mutable.withInitialCapacity(10);
		Set<String> setCapacity = Sets.mutable.withInitialCapacity(10);
		SortedMap<String, Integer> mapComparator = SortedMaps.mutable.with(Comparator.naturalOrder());
		SortedSet<String> setComparator = SortedSets.mutable.with(Comparator.naturalOrder());
		List<String> emptyList = Lists.fixedSize.empty();
		Map<String, Integer> emptyMap = Maps.fixedSize.empty();
		Set<String> emptySet = Sets.fixedSize.empty();
		SortedMap<String, Integer> emptySortedMap = SortedMaps.mutable.empty();
		SortedSet<String> emptySortedSet = SortedSets.mutable.empty();
	}
}
