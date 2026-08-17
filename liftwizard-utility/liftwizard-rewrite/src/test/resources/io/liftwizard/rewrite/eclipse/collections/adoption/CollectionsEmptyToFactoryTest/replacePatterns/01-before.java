import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

class Test
{
	private final List<String> emptyListField = Collections.emptyList();
	private final Set<Integer> emptySetField = Collections.emptySet();
	private final Map<String, Integer> emptyMapField = Collections.emptyMap();
	private final SortedSet<String> emptySortedSetField = Collections.emptySortedSet();
	private final SortedMap<String, Integer> emptySortedMapField = Collections.emptySortedMap();

	private final List<String> emptyListConstructor;
	private final Set<Integer> emptySetConstructor;
	private final Map<String, Integer> emptyMapConstructor;
	private final SortedSet<String> emptySortedSetConstructor;
	private final SortedMap<String, Integer> emptySortedMapConstructor;

	Test()
	{
		this.emptyListConstructor = Collections.emptyList();
		this.emptySetConstructor = Collections.emptySet();
		this.emptyMapConstructor = Collections.emptyMap();
		this.emptySortedSetConstructor = Collections.emptySortedSet();
		this.emptySortedMapConstructor = Collections.emptySortedMap();
	}

	void test()
	{
		List<String> emptyList = Collections.emptyList();
		List<String> emptyListExplicit = Collections.<String>emptyList();
		Set<String> emptySet = Collections.emptySet();
		Set<String> emptySetExplicit = Collections.<String>emptySet();
		Map<String, Integer> emptyMap = Collections.emptyMap();
		Map<String, Integer> emptyMapExplicit = Collections.<String, Integer>emptyMap();
		SortedSet<String> emptySortedSet = Collections.emptySortedSet();
		SortedSet<String> emptySortedSetExplicit = Collections.<String>emptySortedSet();
		SortedMap<String, Integer> emptySortedMap = Collections.emptySortedMap();
		SortedMap<String, Integer> emptySortedMapExplicit = Collections.<String, Integer>emptySortedMap();
	}
}
