import java.util.Map;

import org.eclipse.collections.api.bag.MutableBag;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;
import org.eclipse.collections.api.stack.MutableStack;
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
	private final MutableList<String> fieldList = FastList.newList();
	private final MutableSet<String> fieldSet = UnifiedSet.newSet();
	private final MutableMap<String, Integer> fieldMap = UnifiedMap.newMap();
	private final MutableBag<String> fieldBag = HashBag.newBag();
	private final MutableSortedSet<String> fieldSortedSet = TreeSortedSet.newSet();
	private final MutableSortedMap<String, Integer> fieldSortedMap = TreeSortedMap.newMap();
	private final MutableStack<String> fieldStack = ArrayStack.newStack();
	private final MutableMap<String, Integer> fieldConcurrentMap = ConcurrentHashMap.newMap();
	private final MutableSet<String> fieldMultiReaderSet = MultiReaderUnifiedSet.newSet();
	private final MutableSortedSet<String> fieldSortedSetAdapter = getSortedSetAdapter();

	void testSimpleGeneric()
	{
		MutableList<String> simpleList = FastList.newList();
		MutableSet<String> simpleSet = UnifiedSet.newSet();
		MutableMap<String, Integer> simpleMap = UnifiedMap.newMap();
		MutableBag<String> simpleBag = HashBag.newBag();
		MutableSortedSet<String> simpleSortedSet = TreeSortedSet.newSet();
		MutableSortedMap<String, Integer> simpleSortedMap = TreeSortedMap.newMap();
		MutableStack<String> simpleStack = ArrayStack.newStack();
		MutableMap<String, Integer> simpleConcurrentMap = ConcurrentHashMap.newMap();
		MutableSet<String> simpleMultiReaderSet = MultiReaderUnifiedSet.newSet();
		MutableSortedSet<String> simpleSortedSetAdapter = getSortedSetAdapter();
	}

	void testFullyQualified()
	{
		MutableList<String> fullyQualifiedList = FastList.newList();
		MutableSet<String> fullyQualifiedSet = UnifiedSet.newSet();
		MutableMap<String, Integer> fullyQualifiedMap = UnifiedMap.newMap();
		MutableBag<String> fullyQualifiedBag = HashBag.newBag();
		MutableSortedSet<String> fullyQualifiedSortedSet =
			TreeSortedSet.newSet();
		MutableSortedMap<String, Integer> fullyQualifiedSortedMap =
			TreeSortedMap.newMap();
		MutableStack<String> fullyQualifiedStack = ArrayStack.newStack();
		MutableMap<String, Integer> fullyQualifiedConcurrentMap =
			ConcurrentHashMap.newMap();
		MutableSet<String> fullyQualifiedMultiReaderSet =
			MultiReaderUnifiedSet.newSet();
	}

	void testRawTypes()
	{
		MutableList rawList = FastList.newList();
		MutableSet rawSet = UnifiedSet.newSet();
		MutableMap rawMap = UnifiedMap.newMap();
		MutableBag rawBag = HashBag.newBag();
		MutableSortedSet rawSortedSet = TreeSortedSet.newSet();
		MutableSortedMap rawSortedMap = TreeSortedMap.newMap();
		MutableStack rawStack = ArrayStack.newStack();
		MutableMap rawConcurrentMap = ConcurrentHashMap.newMap();
		MutableSet rawMultiReaderSet = MultiReaderUnifiedSet.newSet();
	}

	void testWildcardGenerics()
	{
		MutableList<? extends Number> wildcardList = FastList.newList();
		MutableSet<? extends Number> wildcardSet = UnifiedSet.newSet();
		MutableMap<String, ? extends Number> wildcardMap = UnifiedMap.newMap();
		MutableBag<? extends Number> wildcardBag = HashBag.newBag();
		MutableSortedSet<? extends Number> wildcardSortedSet = TreeSortedSet.newSet();
		MutableSortedMap<String, ? extends Number> wildcardSortedMap = TreeSortedMap.newMap();
		MutableStack<? extends Number> wildcardStack = ArrayStack.newStack();
	}

	void testNestedGenerics()
	{
		MutableList<Map<String, Integer>> nestedGenericsList = FastList.newList();
	}

	void testMultipleDeclarations()
	{
		MutableList<String> list1 = FastList.newList(),
			list2 = FastList.newListWith("a", "b");
	}

	private SortedSetAdapter<String> getSortedSetAdapter()
	{
		return null;
	}
}
