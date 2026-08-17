import java.util.Comparator;
import org.eclipse.collections.api.bag.MutableBag;
import org.eclipse.collections.api.bag.sorted.MutableSortedBag;
import org.eclipse.collections.api.factory.Bags;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.factory.SortedBags;
import org.eclipse.collections.api.factory.SortedSets;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;
import org.eclipse.collections.impl.bag.mutable.HashBag;
import org.eclipse.collections.impl.bag.sorted.mutable.TreeBag;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet;

class Test
{
	private final MutableList<String> fieldList = Lists.mutable.with("a", "b", "c");
	private final MutableList<String> fieldListFinal = Lists.mutable.with("x", "y");
	private MutableSet<Integer> fieldSet = Sets.mutable.with(1, 2, 3);
	private final MutableSet<String> fieldSetFinal = Sets.mutable.with("p", "q");
	private MutableBag<String> fieldBag = Bags.mutable.with("one", "two");
	private final MutableBag<Integer> fieldBagFinal = Bags.mutable.with(10, 20);
	private MutableSortedSet<String> fieldSortedSet = SortedSets.mutable.with("alpha", "beta");
	private final MutableSortedSet<String> fieldSortedSetFinal = SortedSets.mutable.with("gamma", "delta");
	private MutableSortedBag<String> fieldSortedBag = SortedBags.mutable.with("first", "second");
	private final MutableSortedBag<Integer> fieldSortedBagFinal = SortedBags.mutable.with(100, 200);
	private static MutableList<String> fieldStaticList = Lists.mutable.with("static", "list");
	private static final MutableSet<String> fieldStaticSetFinal = Sets.mutable.with("static", "set");
	protected MutableList<String> fieldProtectedList = Lists.mutable.with("protected", "list");
	public MutableSet<String> fieldPublicSet = Sets.mutable.with("public", "set");
	MutableBag<String> fieldPackagePrivateBag = Bags.mutable.with("package", "bag");
	private MutableList<String> fieldSingleElement = Lists.mutable.with("single");
	private MutableSortedSet<String> fieldSortedSetWithComparator = SortedSets.mutable.with(Comparator.naturalOrder(), "a", "b", "c");
	private final MutableSortedSet<String> fieldSortedSetWithComparatorFinal = SortedSets.mutable.with(Comparator.reverseOrder(), "x", "y", "z");
	private MutableSortedBag<String> fieldSortedBagWithComparator = SortedBags.mutable.with(Comparator.naturalOrder(), "one", "two");
	private final MutableSortedBag<Integer> fieldSortedBagWithComparatorFinal = SortedBags.mutable.with(Comparator.reverseOrder(), 10, 20, 30);
	private static MutableSortedSet<String> fieldStaticSortedSet = SortedSets.mutable.with(Comparator.naturalOrder(), "static", "set");
	private static final MutableSortedBag<String> fieldStaticSortedBagFinal = SortedBags.mutable.with(Comparator.reverseOrder(), "static", "bag");
	protected MutableSortedSet<String> fieldProtectedSortedSet = SortedSets.mutable.with(Comparator.naturalOrder(), "protected", "set");
	public MutableSortedBag<String> fieldPublicSortedBag = SortedBags.mutable.with(Comparator.reverseOrder(), "public", "bag");
	MutableSortedSet<String> fieldPackagePrivateSortedSet = SortedSets.mutable.with(Comparator.naturalOrder(), "package", "set");

	void localVariables()
	{
		String a = "a";
		String b = "b";
		var list = Lists.mutable.with("a", "b", "c");
		var set = Sets.mutable.with("a", "b", "c");
		var bag = Bags.mutable.with("a", "b", "c");
		var sortedSet = SortedSets.mutable.with("a", "b", "c");
		var sortedBag = SortedBags.mutable.with("a", "b", "c");
		var singleElement = Lists.mutable.with("single");
		var numbers = Sets.mutable.with(1, 2, 3, 4, 5);
		var variables = Lists.mutable.with(a, b);
		var multipleList = Lists.mutable.with("x", "y");
		var multipleSet = Sets.mutable.with(1, 2, 3);
		var multipleBag = Bags.mutable.with("p", "q", "r");
		var multipleSortedSet = SortedSets.mutable.with("x", "y");
		var multipleSortedBag = SortedBags.mutable.with("p", "q", "r");
		MutableList<String> typed = Lists.mutable.with("d", "e", "f");
	}

	void localVariablesWithComparator()
	{
		var sortedSetWithComparator = SortedSets.mutable.with(Comparator.naturalOrder(), "a", "b", "c");
		var sortedBagWithComparator = SortedBags.mutable.with(Comparator.reverseOrder(), "x", "y", "z");
		var singleElementSet = SortedSets.mutable.with(Comparator.naturalOrder(), "single");
		var singleElementBag = SortedBags.mutable.with(Comparator.reverseOrder(), "single");
		var twoElementSet = SortedSets.mutable.with(Comparator.naturalOrder(), "first", "second");
		var twoElementBag = SortedBags.mutable.with(Comparator.reverseOrder(), "first", "second");
		var fourElementSet = SortedSets.mutable.with(Comparator.naturalOrder(), "a", "b", "c", "d");
		var fourElementBag = SortedBags.mutable.with(Comparator.reverseOrder(), "a", "b", "c", "d");
		var fiveElementSet = SortedSets.mutable.with(Comparator.naturalOrder(), "a", "b", "c", "d", "e");
		var fiveElementBag = SortedBags.mutable.with(Comparator.reverseOrder(), "a", "b", "c", "d", "e");
	}
}
