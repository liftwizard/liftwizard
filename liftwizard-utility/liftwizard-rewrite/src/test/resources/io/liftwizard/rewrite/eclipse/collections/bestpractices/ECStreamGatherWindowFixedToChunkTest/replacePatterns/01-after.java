import java.util.List;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

class Test
{
	MutableList<String> mutableList;
	ImmutableList<String> immutableList;
	MutableSet<Integer> mutableSet;

	List<List<String>> windowFixedWithMutableList()
	{
		return mutableList.chunk(3);
	}

	List<List<String>> windowFixedWithImmutableList()
	{
		return immutableList.chunk(5);
	}

	List<List<Integer>> windowFixedWithMutableSet()
	{
		return mutableSet.chunk(2);
	}

	List<List<String>> windowFixedWithVariableSize(int batchSize)
	{
		return mutableList.chunk(batchSize);
	}
}
