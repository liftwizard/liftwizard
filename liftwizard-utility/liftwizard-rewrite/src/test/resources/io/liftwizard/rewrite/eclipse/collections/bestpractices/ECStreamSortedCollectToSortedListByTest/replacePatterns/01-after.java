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
		.toSortedListBy(String::length);

	// Lambda
	List<String> result2 = mutableList
		.toSortedListBy((s) -> s.length());

	// ImmutableList
	List<String> result3 = immutableList
		.toSortedListBy(String::length);

	// MutableSet
	List<String> result4 = mutableSet
		.toSortedListBy(String::length);
}
