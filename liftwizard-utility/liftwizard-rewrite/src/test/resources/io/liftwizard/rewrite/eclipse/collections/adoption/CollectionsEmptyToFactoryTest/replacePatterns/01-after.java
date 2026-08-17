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

class Test
{
	private final List<String> emptyListField = Lists.fixedSize.empty();
	private final Set<Integer> emptySetField = Sets.fixedSize.empty();
	private final Map<String, Integer> emptyMapField = Maps.fixedSize.empty();
	private final SortedSet<String> emptySortedSetField = SortedSets.mutable.empty();
	private final SortedMap<String, Integer> emptySortedMapField = SortedMaps.mutable.empty();

	private final List<String> emptyListConstructor;
	private final Set<Integer> emptySetConstructor;
	private final Map<String, Integer> emptyMapConstructor;
	private final SortedSet<String> emptySortedSetConstructor;
	private final SortedMap<String, Integer> emptySortedMapConstructor;

	Test()
	{
		this.emptyListConstructor = Lists.fixedSize.empty();
		this.emptySetConstructor = Sets.fixedSize.empty();
		this.emptyMapConstructor = Maps.fixedSize.empty();
		this.emptySortedSetConstructor = SortedSets.mutable.empty();
		this.emptySortedMapConstructor = SortedMaps.mutable.empty();
	}

	void test()
	{
		List<String> emptyList = Lists.fixedSize.empty();
		List<String> emptyListExplicit = Lists.fixedSize.<String>empty();
		Set<String> emptySet = Sets.fixedSize.empty();
		Set<String> emptySetExplicit = Sets.fixedSize.<String>empty();
		Map<String, Integer> emptyMap = Maps.fixedSize.empty();
		Map<String, Integer> emptyMapExplicit = Maps.fixedSize.<String, Integer>empty();
		SortedSet<String> emptySortedSet = SortedSets.mutable.empty();
		SortedSet<String> emptySortedSetExplicit = SortedSets.mutable.<String>empty();
		SortedMap<String, Integer> emptySortedMap = SortedMaps.mutable.empty();
		SortedMap<String, Integer> emptySortedMapExplicit = SortedMaps.mutable.<String, Integer>empty();
	}
}
