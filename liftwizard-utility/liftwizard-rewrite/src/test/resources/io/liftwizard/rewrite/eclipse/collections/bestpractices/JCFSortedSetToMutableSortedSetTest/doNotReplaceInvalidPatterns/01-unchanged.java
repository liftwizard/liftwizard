import static org.assertj.core.api.Assertions.assertThat;

import java.util.SortedSet;
import java.util.TreeSet;
import org.eclipse.collections.api.factory.SortedSets;

class Test
{
	private final SortedSet<String> assertedField = SortedSets.mutable.empty();

	void test()
	{
		SortedSet<String> treeSet = new TreeSet<>();
		SortedSet<String> asserted = SortedSets.mutable.with("a");
		assertThat(asserted).containsExactly("a");
		assertThat(this.assertedField).isEmpty();
	}
}
