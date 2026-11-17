import static org.assertj.core.api.Assertions.assertThat;

import java.util.SortedMap;
import java.util.TreeMap;
import org.eclipse.collections.api.factory.SortedMaps;

class Test
{
	private final SortedMap<String, Integer> assertedField = SortedMaps.mutable.empty();

	void test()
	{
		SortedMap<String, Integer> treeMap = new TreeMap<>();
		SortedMap<String, Integer> asserted = SortedMaps.mutable.with("a", 1);
		assertThat(asserted).containsEntry("a", 1);
		assertThat(this.assertedField).isEmpty();
	}
}
