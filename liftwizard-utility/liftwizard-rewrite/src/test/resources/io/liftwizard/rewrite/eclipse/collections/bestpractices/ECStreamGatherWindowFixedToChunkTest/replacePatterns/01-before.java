import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Gatherers;
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
		return mutableList.stream().gather(Gatherers.windowFixed(3)).collect(Collectors.toList());
	}

	List<List<String>> windowFixedWithImmutableList()
	{
		return immutableList.stream().gather(Gatherers.windowFixed(5)).collect(Collectors.toList());
	}

	List<List<Integer>> windowFixedWithMutableSet()
	{
		return mutableSet.stream().gather(Gatherers.windowFixed(2)).collect(Collectors.toList());
	}

	List<List<String>> windowFixedWithVariableSize(int batchSize)
	{
		return mutableList.stream().gather(Gatherers.windowFixed(batchSize)).collect(Collectors.toList());
	}
}
