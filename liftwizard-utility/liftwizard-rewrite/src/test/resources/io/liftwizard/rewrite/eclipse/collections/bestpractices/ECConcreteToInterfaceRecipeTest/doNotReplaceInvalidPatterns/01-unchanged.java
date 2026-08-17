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
	FastList<String> methodReturningList()
	{
		return FastList.newList();
	}

	UnifiedSet<String> methodReturningSet()
	{
		return UnifiedSet.newSet();
	}

	UnifiedMap<String, Integer> methodReturningMap()
	{
		return UnifiedMap.newMap();
	}

	HashBag<String> methodReturningBag()
	{
		return HashBag.newBag();
	}

	TreeSortedSet<String> methodReturningSortedSet()
	{
		return TreeSortedSet.newSet();
	}

	TreeSortedMap<String, Integer> methodReturningSortedMap()
	{
		return TreeSortedMap.newMap();
	}

	ArrayStack<String> methodReturningStack()
	{
		return ArrayStack.newStack();
	}

	ConcurrentHashMap<String, Integer> methodReturningConcurrentMap()
	{
		return ConcurrentHashMap.newMap();
	}

	MultiReaderUnifiedSet<String> methodReturningMultiReaderSet()
	{
		return MultiReaderUnifiedSet.newSet();
	}

	SortedSetAdapter<String> methodReturningSortedSetAdapter()
	{
		return null;
	}

	void methodWithListParameter(FastList<String> list)
	{
		list.size();
	}

	void methodWithSetParameter(UnifiedSet<String> set)
	{
		set.size();
	}

	void methodWithMapParameter(UnifiedMap<String, Integer> map)
	{
		map.size();
	}

	void methodWithBagParameter(HashBag<String> bag)
	{
		bag.size();
	}

	void methodWithSortedSetParameter(TreeSortedSet<String> set)
	{
		set.size();
	}

	void methodWithSortedMapParameter(TreeSortedMap<String, Integer> map)
	{
		map.size();
	}

	void methodWithStackParameter(ArrayStack<String> stack)
	{
		stack.size();
	}

	void methodWithConcurrentMapParameter(ConcurrentHashMap<String, Integer> map)
	{
		map.size();
	}

	void methodWithMultiReaderSetParameter(MultiReaderUnifiedSet<String> set)
	{
		set.size();
	}

	void methodWithSortedSetAdapterParameter(SortedSetAdapter<String> set)
	{
		set.size();
	}

	void variablesWithoutInitializer()
	{
		FastList<String> uninitializedList;
		UnifiedSet<String> uninitializedSet;
		UnifiedMap<String, Integer> uninitializedMap;
		HashBag<String> uninitializedBag;
		TreeSortedSet<String> uninitializedSortedSet;
		TreeSortedMap<String, Integer> uninitializedSortedMap;
		ArrayStack<String> uninitializedStack;
		ConcurrentHashMap<String, Integer> uninitializedConcurrentMap;
		MultiReaderUnifiedSet<String> uninitializedMultiReaderSet;
		SortedSetAdapter<String> uninitializedSortedSetAdapter;
	}

	void nonFinalFields()
	{
		class Inner
		{
			private FastList<String> nonFinalList = FastList.newList();
			private UnifiedSet<String> nonFinalSet = UnifiedSet.newSet();
			private UnifiedMap<String, Integer> nonFinalMap = UnifiedMap.newMap();
			private HashBag<String> nonFinalBag = HashBag.newBag();
			private TreeSortedSet<String> nonFinalSortedSet = TreeSortedSet.newSet();
			private TreeSortedMap<String, Integer> nonFinalSortedMap = TreeSortedMap.newMap();
			private ArrayStack<String> nonFinalStack = ArrayStack.newStack();
			private ConcurrentHashMap<String, Integer> nonFinalConcurrentMap = ConcurrentHashMap.newMap();
			private MultiReaderUnifiedSet<String> nonFinalMultiReaderSet = MultiReaderUnifiedSet.newSet();
		}
	}
}

interface MyListInterface
	extends FastList<String>
{
}

class GenericBoundsExample<T extends FastList<String>>
{
}
