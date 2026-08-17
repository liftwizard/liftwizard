import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	MutableList<String> mutableList;
	ArrayList<String> arrayList;
	Predicate<String> predicate;

	// toUnmodifiableList - unmodifiable != immutable
	List<String> invalid1 = mutableList.stream().filter(predicate).collect(Collectors.toUnmodifiableList());

	// toUnmodifiableSet - unmodifiable != immutable
	Set<String> invalid2 = mutableList.stream().filter(predicate).collect(Collectors.toUnmodifiableSet());

	// Only stream
	Stream<String> invalid3 = mutableList.stream();

	// Only filter without collect
	Stream<String> invalid4 = mutableList.stream().filter(String::isEmpty);

	// Non-Eclipse Collections type
	List<String> invalid5 = arrayList.stream().filter(predicate).collect(Collectors.toList());

	// Multiple intermediate operations
	List<String> invalid6()
	{
		return mutableList
			.stream()
			.filter((s) -> s.length() > 3)
			.map(String::toUpperCase)
			.collect(Collectors.toList());
	}
}
