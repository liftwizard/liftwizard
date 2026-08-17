import java.util.Comparator;
import java.util.List;

import org.eclipse.collections.api.factory.SortedSets;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;

class Test<T extends Comparable<T>>
{
	private final MutableSortedSet<String> fieldInterfaceEmpty = SortedSets.mutable.empty();
	private final MutableSortedSet<String> fieldInterfaceComparator = SortedSets.mutable.with(Comparator.naturalOrder());
	private final MutableSortedSet<String> fieldInterfaceIterable = SortedSets.mutable.withAll(Comparator.naturalOrder(), fieldInterfaceEmpty);

	void test()
	{
		MutableSortedSet<String> diamondSet = SortedSets.mutable.empty();
		MutableSortedSet<String> explicitSimple = SortedSets.mutable.<String>empty();
		MutableSortedSet<List<String>> explicitNested = SortedSets.mutable.<List<String>>empty();
		MutableSortedSet<MutableSortedSet<T>> nestedTypeParam = SortedSets.mutable.<MutableSortedSet<T>>empty();
		org.eclipse.collections.api.set.sorted.MutableSortedSet<String> fullyQualified =
				SortedSets.mutable.empty();
		MutableSortedSet<String> withComparator = SortedSets.mutable.with(Comparator.naturalOrder());
		MutableSortedSet<String> withComparatorAndIterable = SortedSets.mutable.withAll(Comparator.reverseOrder(), diamondSet);
	}
}

class A<T extends Comparable<T>>
{
	@Override
	public MutableSortedSet<T> newEmpty()
	{
		return SortedSets.mutable.empty();
	}
}
