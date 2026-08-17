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
	List<String> result1 = mutableList.select(predicate);

	// toSet
	Set<String> result2 = mutableList.select(predicate).toSet();

	// Lambda predicate
	List<String> result3 = mutableList
		.select((s) -> s.length() > 5);

	// Method reference
	List<String> result4 = mutableList.select(String::isEmpty);

	// ImmutableList
	List<String> result5 = immutableList.select(predicate);

	// MutableSet
	Set<Integer> result6 = mutableSet.select(intPredicate).toSet();
}
