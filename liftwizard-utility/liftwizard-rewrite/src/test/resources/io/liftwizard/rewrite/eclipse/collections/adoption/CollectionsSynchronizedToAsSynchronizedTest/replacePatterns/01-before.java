import java.util.Collections;
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
	private final List<String> fieldSynchronizedList = Collections.synchronizedList(fieldList);
	private final Set<String> fieldSynchronizedSet = Collections.synchronizedSet(Sets.mutable.with("x", "y"));
	private final Map<String, Integer> fieldSynchronizedMap = Collections.synchronizedMap(Maps.mutable.with("key", 1));
	private final SortedSet<String> fieldSynchronizedSortedSet = Collections.synchronizedSortedSet(
		SortedSets.mutable.with("x", "y")
	);
	private final SortedMap<String, Integer> fieldSynchronizedSortedMap = Collections.synchronizedSortedMap(
		SortedMaps.mutable.with("key", 1)
	);

	void test(Object obj)
	{
		MutableList<String> list = Lists.mutable.with("a", "b");
		List<String> synchronizedList = Collections.synchronizedList(list);

		MutableSet<String> set = Sets.mutable.with("x", "y");
		Set<String> synchronizedSet = Collections.synchronizedSet(set);

		MutableMap<String, Integer> map = Maps.mutable.with("key", 1);
		Map<String, Integer> synchronizedMap = Collections.synchronizedMap(map);

		MutableSortedMap<String, Integer> sortedMap = SortedMaps.mutable.with("key", 1);
		SortedMap<String, Integer> synchronizedSortedMap = Collections.synchronizedSortedMap(sortedMap);

		MutableSortedSet<String> sortedSet = SortedSets.mutable.with("x", "y");
		SortedSet<String> synchronizedSortedSet = Collections.synchronizedSortedSet(sortedSet);

		List<String> inlineExpression = Collections.synchronizedList(Lists.mutable.with("c", "d"));

		MutableMap<String, Integer> anotherMap = Maps.mutable.with("key", 1);
		Map<String, Integer> result = Collections.synchronizedMap(anotherMap);
		result.put("another", 2);

		MutableList<String> explicitList = Lists.mutable.with((String) obj);
		List<String> explicitSynchronizedList = Collections.<String>synchronizedList(explicitList);

		MutableSet<String> explicitSet = Sets.mutable.with((String) obj);
		Set<String> explicitSynchronizedSet = Collections.<String>synchronizedSet(explicitSet);

		MutableMap<String, Integer> explicitMap = Maps.mutable.with((String) obj, (Integer) obj);
		Map<String, Integer> explicitSynchronizedMap = Collections.<String, Integer>synchronizedMap(explicitMap);

		MutableSortedMap<String, Integer> explicitSortedMap = SortedMaps.mutable.with((String) obj, (Integer) obj);
		SortedMap<String, Integer> explicitSynchronizedSortedMap = Collections.<String, Integer>synchronizedSortedMap(
			explicitSortedMap
		);

		MutableSortedSet<String> explicitSortedSet = SortedSets.mutable.with((String) obj);
		SortedSet<String> explicitSynchronizedSortedSet = Collections.<String>synchronizedSortedSet(explicitSortedSet);
	}
}
