import java.util.Comparator;
import java.util.List;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;
import org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet;

class Test<T extends Comparable<T>>
{
	private final MutableSortedSet<String> fieldInterfaceEmpty = new TreeSortedSet<>();
	private final MutableSortedSet<String> fieldInterfaceComparator = new TreeSortedSet<>(Comparator.naturalOrder());
	private final MutableSortedSet<String> fieldInterfaceIterable = new TreeSortedSet<>(
		Comparator.naturalOrder(),
		fieldInterfaceEmpty
	);

	void test()
	{
		MutableSortedSet<String> diamondSet = new TreeSortedSet<>();
		MutableSortedSet<String> explicitSimple = new TreeSortedSet<String>();
		MutableSortedSet<List<String>> explicitNested = new TreeSortedSet<List<String>>();
		MutableSortedSet<MutableSortedSet<T>> nestedTypeParam = new TreeSortedSet<MutableSortedSet<T>>();
		org.eclipse.collections.api.set.sorted.MutableSortedSet<String> fullyQualified =
			new org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet<>();
		MutableSortedSet<String> withComparator = new TreeSortedSet<>(Comparator.naturalOrder());
		MutableSortedSet<String> withComparatorAndIterable = new TreeSortedSet<>(Comparator.reverseOrder(), diamondSet);
	}
}

class A<T extends Comparable<T>>
{
	@Override
	public MutableSortedSet<T> newEmpty()
	{
		return new TreeSortedSet<>();
	}
}
