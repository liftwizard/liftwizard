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
	List<Integer> result1 = mutableList.stream().map(function).collect(Collectors.toList());

	// toSet
	Set<Integer> result2 = mutableList.stream().map(function).collect(Collectors.toSet());

	// Lambda function
	List<Integer> result3 = mutableList
		.stream()
		.map((s) -> s.length())
		.collect(Collectors.toList());

	// Method reference
	List<Integer> result4 = mutableList.stream().map(String::length).collect(Collectors.toList());

	// ImmutableList
	List<Integer> result5 = immutableList.stream().map(function).collect(Collectors.toList());

	// MutableSet
	Set<Integer> result6 = mutableSet.stream().map(function).collect(Collectors.toSet());
}
