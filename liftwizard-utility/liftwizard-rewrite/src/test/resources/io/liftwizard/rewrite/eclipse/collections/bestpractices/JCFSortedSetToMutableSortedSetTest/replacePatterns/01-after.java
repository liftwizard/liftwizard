import java.util.List;
import org.eclipse.collections.api.factory.SortedSets;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;
import org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet;

class Test
{
	private final MutableSortedSet<String> fieldSortedSet = SortedSets.mutable.empty();

	void test()
	{
		MutableSortedSet<String> simpleSortedSet = SortedSets.mutable.empty();
		MutableSortedSet<String> fullyQualifiedSortedSet = SortedSets.mutable.empty();
		MutableSortedSet rawSortedSet = SortedSets.mutable.empty();
		MutableSortedSet rawSortedSetFullyQualified = SortedSets.mutable.empty();
		MutableSortedSet<List<Integer>> nestedGenerics = SortedSets.mutable.empty();
		MutableSortedSet<String> treeSortedSet = TreeSortedSet.newSet();
		MutableSortedSet<String> set1 = SortedSets.mutable.empty(),
			set2 = SortedSets.mutable.with("a");
	}
}
