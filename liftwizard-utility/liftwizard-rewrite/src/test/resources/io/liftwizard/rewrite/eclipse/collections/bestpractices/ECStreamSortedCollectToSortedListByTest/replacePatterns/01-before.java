import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

class Test
{
	MutableList<String> mutableList;
	ImmutableList<String> immutableList;
	MutableSet<String> mutableSet;

	// Method reference
	List<String> result1 = mutableList
		.stream()
		.sorted(Comparator.comparing(String::length))
		.collect(Collectors.toList());

	// Lambda
	List<String> result2 = mutableList
		.stream()
		.sorted(Comparator.comparing((s) -> s.length()))
		.collect(Collectors.toList());

	// ImmutableList
	List<String> result3 = immutableList
		.stream()
		.sorted(Comparator.comparing(String::length))
		.collect(Collectors.toList());

	// MutableSet
	List<String> result4 = mutableSet
		.stream()
		.sorted(Comparator.comparing(String::length))
		.collect(Collectors.toList());
}
