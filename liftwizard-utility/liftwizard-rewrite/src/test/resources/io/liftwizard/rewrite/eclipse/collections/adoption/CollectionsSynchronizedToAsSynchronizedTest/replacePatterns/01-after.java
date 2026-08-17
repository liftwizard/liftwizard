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
	private final List<String> fieldSynchronizedList = fieldList.asSynchronized();
	private final Set<String> fieldSynchronizedSet = Sets.mutable.with("x", "y").asSynchronized();
	private final Map<String, Integer> fieldSynchronizedMap = Maps.mutable.with("key", 1).asSynchronized();
	private final SortedSet<String> fieldSynchronizedSortedSet = SortedSets.mutable.with("x", "y").asSynchronized();
	private final SortedMap<String, Integer> fieldSynchronizedSortedMap = SortedMaps.mutable.with("key", 1).asSynchronized();

	void test(Object obj)
	{
		MutableList<String> list = Lists.mutable.with("a", "b");
		List<String> synchronizedList = list.asSynchronized();

		MutableSet<String> set = Sets.mutable.with("x", "y");
		Set<String> synchronizedSet = set.asSynchronized();

		MutableMap<String, Integer> map = Maps.mutable.with("key", 1);
		Map<String, Integer> synchronizedMap = map.asSynchronized();

		MutableSortedMap<String, Integer> sortedMap = SortedMaps.mutable.with("key", 1);
		SortedMap<String, Integer> synchronizedSortedMap = sortedMap.asSynchronized();

		MutableSortedSet<String> sortedSet = SortedSets.mutable.with("x", "y");
		SortedSet<String> synchronizedSortedSet = sortedSet.asSynchronized();

		List<String> inlineExpression = Lists.mutable.with("c", "d").asSynchronized();

		MutableMap<String, Integer> anotherMap = Maps.mutable.with("key", 1);
		Map<String, Integer> result = anotherMap.asSynchronized();
		result.put("another", 2);

		MutableList<String> explicitList = Lists.mutable.with((String) obj);
		List<String> explicitSynchronizedList = explicitList.asSynchronized();

		MutableSet<String> explicitSet = Sets.mutable.with((String) obj);
		Set<String> explicitSynchronizedSet = explicitSet.asSynchronized();

		MutableMap<String, Integer> explicitMap = Maps.mutable.with((String) obj, (Integer) obj);
		Map<String, Integer> explicitSynchronizedMap = explicitMap.asSynchronized();

		MutableSortedMap<String, Integer> explicitSortedMap = SortedMaps.mutable.with((String) obj, (Integer) obj);
		SortedMap<String, Integer> explicitSynchronizedSortedMap = explicitSortedMap.asSynchronized();

		MutableSortedSet<String> explicitSortedSet = SortedSets.mutable.with((String) obj);
		SortedSet<String> explicitSynchronizedSortedSet = explicitSortedSet.asSynchronized();
	}
}
