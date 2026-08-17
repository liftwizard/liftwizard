import java.util.Arrays;
import java.util.Comparator;
import org.eclipse.collections.api.bag.MutableBag;
import org.eclipse.collections.api.bag.sorted.MutableSortedBag;
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
	private final MutableList<String> fieldList = FastList.newList(Arrays.asList("a", "b", "c"));
	private final MutableList<String> fieldListFinal = FastList.newList(Arrays.asList("x", "y"));
	private MutableSet<Integer> fieldSet = UnifiedSet.newSet(Arrays.asList(1, 2, 3));
	private final MutableSet<String> fieldSetFinal = UnifiedSet.newSet(Arrays.asList("p", "q"));
	private MutableBag<String> fieldBag = HashBag.newBag(Arrays.asList("one", "two"));
	private final MutableBag<Integer> fieldBagFinal = HashBag.newBag(Arrays.asList(10, 20));
	private MutableSortedSet<String> fieldSortedSet = TreeSortedSet.newSet(Arrays.asList("alpha", "beta"));
	private final MutableSortedSet<String> fieldSortedSetFinal = TreeSortedSet.newSet(Arrays.asList("gamma", "delta"));
	private MutableSortedBag<String> fieldSortedBag = TreeBag.newBag(Arrays.asList("first", "second"));
	private final MutableSortedBag<Integer> fieldSortedBagFinal = TreeBag.newBag(Arrays.asList(100, 200));
	private static MutableList<String> fieldStaticList = FastList.newList(Arrays.asList("static", "list"));
	private static final MutableSet<String> fieldStaticSetFinal = UnifiedSet.newSet(Arrays.asList("static", "set"));
	protected MutableList<String> fieldProtectedList = FastList.newList(Arrays.asList("protected", "list"));
	public MutableSet<String> fieldPublicSet = UnifiedSet.newSet(Arrays.asList("public", "set"));
	MutableBag<String> fieldPackagePrivateBag = HashBag.newBag(Arrays.asList("package", "bag"));
	private MutableList<String> fieldSingleElement = FastList.newList(Arrays.asList("single"));
	private MutableSortedSet<String> fieldSortedSetWithComparator = TreeSortedSet.newSet(
		Comparator.naturalOrder(),
		Arrays.asList("a", "b", "c")
	);
	private final MutableSortedSet<String> fieldSortedSetWithComparatorFinal = TreeSortedSet.newSet(
		Comparator.reverseOrder(),
		Arrays.asList("x", "y", "z")
	);
	private MutableSortedBag<String> fieldSortedBagWithComparator = TreeBag.newBag(
		Comparator.naturalOrder(),
		Arrays.asList("one", "two")
	);
	private final MutableSortedBag<Integer> fieldSortedBagWithComparatorFinal = TreeBag.newBag(
		Comparator.reverseOrder(),
		Arrays.asList(10, 20, 30)
	);
	private static MutableSortedSet<String> fieldStaticSortedSet = TreeSortedSet.newSet(
		Comparator.naturalOrder(),
		Arrays.asList("static", "set")
	);
	private static final MutableSortedBag<String> fieldStaticSortedBagFinal = TreeBag.newBag(
		Comparator.reverseOrder(),
		Arrays.asList("static", "bag")
	);
	protected MutableSortedSet<String> fieldProtectedSortedSet = TreeSortedSet.newSet(
		Comparator.naturalOrder(),
		Arrays.asList("protected", "set")
	);
	public MutableSortedBag<String> fieldPublicSortedBag = TreeBag.newBag(
		Comparator.reverseOrder(),
		Arrays.asList("public", "bag")
	);
	MutableSortedSet<String> fieldPackagePrivateSortedSet = TreeSortedSet.newSet(
		Comparator.naturalOrder(),
		Arrays.asList("package", "set")
	);

	void localVariables()
	{
		String a = "a";
		String b = "b";
		var list = FastList.newList(Arrays.asList("a", "b", "c"));
		var set = UnifiedSet.newSet(Arrays.asList("a", "b", "c"));
		var bag = HashBag.newBag(Arrays.asList("a", "b", "c"));
		var sortedSet = TreeSortedSet.newSet(Arrays.asList("a", "b", "c"));
		var sortedBag = TreeBag.newBag(Arrays.asList("a", "b", "c"));
		var singleElement = FastList.newList(Arrays.asList("single"));
		var numbers = UnifiedSet.newSet(Arrays.asList(1, 2, 3, 4, 5));
		var variables = FastList.newList(Arrays.asList(a, b));
		var multipleList = FastList.newList(Arrays.asList("x", "y"));
		var multipleSet = UnifiedSet.newSet(Arrays.asList(1, 2, 3));
		var multipleBag = HashBag.newBag(Arrays.asList("p", "q", "r"));
		var multipleSortedSet = TreeSortedSet.newSet(Arrays.asList("x", "y"));
		var multipleSortedBag = TreeBag.newBag(Arrays.asList("p", "q", "r"));
		MutableList<String> typed = FastList.newList(Arrays.asList("d", "e", "f"));
	}

	void localVariablesWithComparator()
	{
		var sortedSetWithComparator = TreeSortedSet.newSet(Comparator.naturalOrder(), Arrays.asList("a", "b", "c"));
		var sortedBagWithComparator = TreeBag.newBag(Comparator.reverseOrder(), Arrays.asList("x", "y", "z"));
		var singleElementSet = TreeSortedSet.newSet(Comparator.naturalOrder(), Arrays.asList("single"));
		var singleElementBag = TreeBag.newBag(Comparator.reverseOrder(), Arrays.asList("single"));
		var twoElementSet = TreeSortedSet.newSet(Comparator.naturalOrder(), Arrays.asList("first", "second"));
		var twoElementBag = TreeBag.newBag(Comparator.reverseOrder(), Arrays.asList("first", "second"));
		var fourElementSet = TreeSortedSet.newSet(Comparator.naturalOrder(), Arrays.asList("a", "b", "c", "d"));
		var fourElementBag = TreeBag.newBag(Comparator.reverseOrder(), Arrays.asList("a", "b", "c", "d"));
		var fiveElementSet = TreeSortedSet.newSet(Comparator.naturalOrder(), Arrays.asList("a", "b", "c", "d", "e"));
		var fiveElementBag = TreeBag.newBag(Comparator.reverseOrder(), Arrays.asList("a", "b", "c", "d", "e"));
	}
}
