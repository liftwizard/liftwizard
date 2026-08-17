import java.util.List;
import java.util.SortedSet;
import org.eclipse.collections.api.factory.SortedSets;
import org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet;

class Test
{
	private final SortedSet<String> fieldSortedSet = SortedSets.mutable.empty();

	void test()
	{
		SortedSet<String> simpleSortedSet = SortedSets.mutable.empty();
		java.util.SortedSet<String> fullyQualifiedSortedSet = SortedSets.mutable.empty();
		SortedSet rawSortedSet = SortedSets.mutable.empty();
		java.util.SortedSet rawSortedSetFullyQualified = SortedSets.mutable.empty();
		SortedSet<List<Integer>> nestedGenerics = SortedSets.mutable.empty();
		SortedSet<String> treeSortedSet = TreeSortedSet.newSet();
		SortedSet<String> set1 = SortedSets.mutable.empty(),
			set2 = SortedSets.mutable.with("a");
	}
}
