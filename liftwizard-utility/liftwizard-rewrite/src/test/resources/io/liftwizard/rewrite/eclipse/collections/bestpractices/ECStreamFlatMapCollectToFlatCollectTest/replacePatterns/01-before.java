import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

class Test
{
	MutableList<MutableList<String>> mutableList;
	ImmutableList<MutableList<String>> immutableList;
	MutableSet<MutableList<String>> mutableSet;

	// Lambda with toList
	List<String> result1 = mutableList
		.stream()
		.flatMap((x) -> x.stream())
		.collect(Collectors.toList());

	// Lambda with toSet
	Set<String> result2 = mutableList
		.stream()
		.flatMap((x) -> x.stream())
		.collect(Collectors.toSet());

	// Lambda with method call then stream
	List<String> result3 = mutableList
		.stream()
		.flatMap((x) -> x.subList(0, 1).stream())
		.collect(Collectors.toList());

	// ImmutableList
	List<String> result4 = immutableList
		.stream()
		.flatMap((x) -> x.stream())
		.collect(Collectors.toList());

	// MutableSet with toSet
	Set<String> result5 = mutableSet
		.stream()
		.flatMap((x) -> x.stream())
		.collect(Collectors.toSet());
}
