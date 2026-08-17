import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	MutableList<String> mutableList;
	ArrayList<String> arrayList;
	Function<String, Integer> function;

	// toUnmodifiableList - unmodifiable != immutable
	List<Integer> invalid1 = mutableList.stream().map(function).collect(Collectors.toUnmodifiableList());

	// toUnmodifiableSet - unmodifiable != immutable
	Set<Integer> invalid2 = mutableList.stream().map(function).collect(Collectors.toUnmodifiableSet());

	// Only stream
	Stream<String> invalid3 = mutableList.stream();

	// Only map without collect
	Stream<Integer> invalid4 = mutableList.stream().map(String::length);

	// Non-Eclipse Collections type
	List<Integer> invalid5 = arrayList.stream().map(function).collect(Collectors.toList());

	// Multiple intermediate operations
	List<String> invalid6()
	{
		return mutableList
			.stream()
			.map(String::toUpperCase)
			.filter((s) -> s.length() > 3)
			.collect(Collectors.toList());
	}
}
