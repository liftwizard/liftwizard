import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap;

class Test
{
	private final TreeSortedMap<String, String> fieldConcreteType = new TreeSortedMap<>();

	void test()
	{
		Map<String, Integer> regularMap = new HashMap<>();
		TreeSortedMap<String, Integer> concreteTypeEmpty = new TreeSortedMap<>();
		TreeSortedMap<String, Integer> concreteTypeComparator = new TreeSortedMap<>(Comparator.naturalOrder());
		TreeSortedMap<String, Integer> concreteTypeMap = new TreeSortedMap<>(regularMap);
	}
}
