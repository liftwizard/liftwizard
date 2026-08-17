import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	MutableList<String> mutableList;
	ArrayList<String> arrayList;

	// stream().toList() - unmodifiable != mutable
	List<String> invalid1 = mutableList.stream().toList();

	// toUnmodifiableList - unmodifiable != mutable
	List<String> invalid2 = mutableList.stream().collect(Collectors.toUnmodifiableList());

	// toSet - different recipe handles this
	Set<String> invalid3 = mutableList.stream().collect(Collectors.toSet());

	// Only stream without collect
	Stream<String> invalid4 = mutableList.stream();

	// Non-Eclipse Collections type
	List<String> invalid5 = arrayList.stream().collect(Collectors.toList());

	// Intermediate operations (filter)
	List<String> invalid6()
	{
		return mutableList
			.stream()
			.filter((s) -> s.length() > 3)
			.collect(Collectors.toList());
	}
}
