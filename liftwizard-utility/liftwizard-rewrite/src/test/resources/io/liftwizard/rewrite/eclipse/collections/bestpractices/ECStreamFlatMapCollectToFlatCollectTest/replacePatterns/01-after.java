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
		.flatCollect((x) -> x);

	// Lambda with toSet
	Set<String> result2 = mutableList
		.flatCollect((x) -> x)
		.toSet();

	// Lambda with method call then stream
	List<String> result3 = mutableList
		.flatCollect((x) -> x.subList(0, 1));

	// ImmutableList
	List<String> result4 = immutableList
		.flatCollect((x) -> x);

	// MutableSet with toSet
	Set<String> result5 = mutableSet
		.flatCollect((x) -> x)
		.toSet();
}
