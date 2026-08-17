import java.util.Map;
import org.eclipse.collections.impl.bag.mutable.HashBag;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap;
import org.eclipse.collections.impl.set.mutable.MultiReaderUnifiedSet;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.collections.impl.set.sorted.mutable.SortedSetAdapter;
import org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet;
import org.eclipse.collections.impl.stack.mutable.ArrayStack;

class Test
{
	private final FastList<String> fieldList = FastList.newList();
	private final UnifiedSet<String> fieldSet = UnifiedSet.newSet();
	private final UnifiedMap<String, Integer> fieldMap = UnifiedMap.newMap();
	private final HashBag<String> fieldBag = HashBag.newBag();
	private final TreeSortedSet<String> fieldSortedSet = TreeSortedSet.newSet();
	private final TreeSortedMap<String, Integer> fieldSortedMap = TreeSortedMap.newMap();
	private final ArrayStack<String> fieldStack = ArrayStack.newStack();
	private final ConcurrentHashMap<String, Integer> fieldConcurrentMap = ConcurrentHashMap.newMap();
	private final MultiReaderUnifiedSet<String> fieldMultiReaderSet = MultiReaderUnifiedSet.newSet();
	private final SortedSetAdapter<String> fieldSortedSetAdapter = getSortedSetAdapter();

	void testSimpleGeneric()
	{
		FastList<String> simpleList = FastList.newList();
		UnifiedSet<String> simpleSet = UnifiedSet.newSet();
		UnifiedMap<String, Integer> simpleMap = UnifiedMap.newMap();
		HashBag<String> simpleBag = HashBag.newBag();
		TreeSortedSet<String> simpleSortedSet = TreeSortedSet.newSet();
		TreeSortedMap<String, Integer> simpleSortedMap = TreeSortedMap.newMap();
		ArrayStack<String> simpleStack = ArrayStack.newStack();
		ConcurrentHashMap<String, Integer> simpleConcurrentMap = ConcurrentHashMap.newMap();
		MultiReaderUnifiedSet<String> simpleMultiReaderSet = MultiReaderUnifiedSet.newSet();
		SortedSetAdapter<String> simpleSortedSetAdapter = getSortedSetAdapter();
	}

	void testFullyQualified()
	{
		org.eclipse.collections.impl.list.mutable.FastList<String> fullyQualifiedList = FastList.newList();
		org.eclipse.collections.impl.set.mutable.UnifiedSet<String> fullyQualifiedSet = UnifiedSet.newSet();
		org.eclipse.collections.impl.map.mutable.UnifiedMap<String, Integer> fullyQualifiedMap = UnifiedMap.newMap();
		org.eclipse.collections.impl.bag.mutable.HashBag<String> fullyQualifiedBag = HashBag.newBag();
		org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet<String> fullyQualifiedSortedSet =
			TreeSortedSet.newSet();
		org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap<String, Integer> fullyQualifiedSortedMap =
			TreeSortedMap.newMap();
		org.eclipse.collections.impl.stack.mutable.ArrayStack<String> fullyQualifiedStack = ArrayStack.newStack();
		org.eclipse.collections.impl.map.mutable.ConcurrentHashMap<String, Integer> fullyQualifiedConcurrentMap =
			ConcurrentHashMap.newMap();
		org.eclipse.collections.impl.set.mutable.MultiReaderUnifiedSet<String> fullyQualifiedMultiReaderSet =
			MultiReaderUnifiedSet.newSet();
	}

	void testRawTypes()
	{
		FastList rawList = FastList.newList();
		UnifiedSet rawSet = UnifiedSet.newSet();
		UnifiedMap rawMap = UnifiedMap.newMap();
		HashBag rawBag = HashBag.newBag();
		TreeSortedSet rawSortedSet = TreeSortedSet.newSet();
		TreeSortedMap rawSortedMap = TreeSortedMap.newMap();
		ArrayStack rawStack = ArrayStack.newStack();
		ConcurrentHashMap rawConcurrentMap = ConcurrentHashMap.newMap();
		MultiReaderUnifiedSet rawMultiReaderSet = MultiReaderUnifiedSet.newSet();
	}

	void testWildcardGenerics()
	{
		FastList<? extends Number> wildcardList = FastList.newList();
		UnifiedSet<? extends Number> wildcardSet = UnifiedSet.newSet();
		UnifiedMap<String, ? extends Number> wildcardMap = UnifiedMap.newMap();
		HashBag<? extends Number> wildcardBag = HashBag.newBag();
		TreeSortedSet<? extends Number> wildcardSortedSet = TreeSortedSet.newSet();
		TreeSortedMap<String, ? extends Number> wildcardSortedMap = TreeSortedMap.newMap();
		ArrayStack<? extends Number> wildcardStack = ArrayStack.newStack();
	}

	void testNestedGenerics()
	{
		FastList<Map<String, Integer>> nestedGenericsList = FastList.newList();
	}

	void testMultipleDeclarations()
	{
		FastList<String> list1 = FastList.newList(),
			list2 = FastList.newListWith("a", "b");
	}

	private SortedSetAdapter<String> getSortedSetAdapter()
	{
		return null;
	}
}
