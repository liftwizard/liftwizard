import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

class Test
{
	MutableList<String> mutableList;
	ImmutableList<String> immutableList;
	MutableSet<Integer> mutableSet;

	// MutableList
	List<String> result1 = mutableList.toList();

	// ImmutableList
	List<String> result2 = immutableList.toList();

	// MutableSet
	List<Integer> result3 = mutableSet.toList();
}
