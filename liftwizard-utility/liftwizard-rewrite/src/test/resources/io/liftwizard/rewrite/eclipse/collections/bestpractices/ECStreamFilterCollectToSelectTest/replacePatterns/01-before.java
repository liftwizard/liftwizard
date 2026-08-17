import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

class Test
{
	MutableList<String> mutableList;
	ImmutableList<String> immutableList;
	MutableSet<Integer> mutableSet;
	Predicate<String> predicate;
	Predicate<Integer> intPredicate;

	// Predicate variable with toList
	List<String> result1 = mutableList.stream().filter(predicate).collect(Collectors.toList());

	// toSet
	Set<String> result2 = mutableList.stream().filter(predicate).collect(Collectors.toSet());

	// Lambda predicate
	List<String> result3 = mutableList
		.stream()
		.filter((s) -> s.length() > 5)
		.collect(Collectors.toList());

	// Method reference
	List<String> result4 = mutableList.stream().filter(String::isEmpty).collect(Collectors.toList());

	// ImmutableList
	List<String> result5 = immutableList.stream().filter(predicate).collect(Collectors.toList());

	// MutableSet
	Set<Integer> result6 = mutableSet.stream().filter(intPredicate).collect(Collectors.toSet());
}
