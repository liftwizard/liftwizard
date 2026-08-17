import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

class Test
{
	MutableList<String> mutableList;
	ImmutableList<String> immutableList;
	MutableSet<String> mutableSet;
	Function<String, Integer> function;

	// Function variable with toList
	List<Integer> result1 = mutableList.collect(function);

	// toSet
	Set<Integer> result2 = mutableList.collect(function).toSet();

	// Lambda function
	List<Integer> result3 = mutableList
		.collect((s) -> s.length());

	// Method reference
	List<Integer> result4 = mutableList.collect(String::length);

	// ImmutableList
	List<Integer> result5 = immutableList.collect(function);

	// MutableSet
	Set<Integer> result6 = mutableSet.collect(function).toSet();
}
