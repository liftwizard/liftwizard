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

		List<String> listCopy = new ArrayList<>(list);
		Map<String, Integer> mapCopy = new HashMap<>(map);
		Set<String> setCopy = new HashSet<>(set);
		SortedMap<String, Integer> sortedMapCopy = new TreeMap<>(sortedMap);
		SortedSet<String> sortedSetCopy = new TreeSet<>(sortedSet);
		List<String> listCapacity = new ArrayList<>(10);
		Map<String, Integer> mapCapacity = new HashMap<>(10);
		Set<String> setCapacity = new HashSet<>(10);
		SortedMap<String, Integer> mapComparator = new TreeMap<>(Comparator.naturalOrder());
		SortedSet<String> setComparator = new TreeSet<>(Comparator.naturalOrder());
		List<String> emptyList = Collections.emptyList();
		Map<String, Integer> emptyMap = Collections.emptyMap();
		Set<String> emptySet = Collections.emptySet();
		SortedMap<String, Integer> emptySortedMap = Collections.emptySortedMap();
		SortedSet<String> emptySortedSet = Collections.emptySortedSet();
	}
}
