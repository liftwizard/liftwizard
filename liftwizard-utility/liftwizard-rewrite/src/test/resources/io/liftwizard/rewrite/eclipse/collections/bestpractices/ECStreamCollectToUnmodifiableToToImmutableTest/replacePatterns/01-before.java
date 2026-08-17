import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;

class Test
{
	MutableList<String> mutableList;
	ImmutableList<String> immutableList;
	MutableSet<Integer> mutableSet;
	ImmutableSet<Integer> immutableSet;

	// toUnmodifiableList - MutableList
	List<String> result1 = mutableList.stream().collect(Collectors.toUnmodifiableList());

	// toUnmodifiableList - ImmutableList
	List<String> result2 = immutableList.stream().collect(Collectors.toUnmodifiableList());

	// toUnmodifiableList - MutableSet
	List<Integer> result3 = mutableSet.stream().collect(Collectors.toUnmodifiableList());

	// toUnmodifiableSet - MutableList
	Set<String> result4 = mutableList.stream().collect(Collectors.toUnmodifiableSet());

	// toUnmodifiableSet - MutableSet
	Set<Integer> result5 = mutableSet.stream().collect(Collectors.toUnmodifiableSet());

	// toUnmodifiableSet - ImmutableSet
	Set<Integer> result6 = immutableSet.stream().collect(Collectors.toUnmodifiableSet());
}
