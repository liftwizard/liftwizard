import java.util.Comparator;
import java.util.stream.Collectors;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

class Test
{
	MutableList<String> mutableList;
	ImmutableList<Integer> immutableList;
	MutableSet<String> mutableSet;

	// Method reference
	String minByMethodRef = mutableList
		.stream()
		.collect(Collectors.minBy(Comparator.comparing(String::length)))
		.orElse(null);
	String maxByMethodRef = mutableList
		.stream()
		.collect(Collectors.maxBy(Comparator.comparing(String::length)))
		.orElse(null);

	// Lambda
	String minByLambda = mutableList
		.stream()
		.collect(Collectors.minBy(Comparator.comparing((s) -> s.length())))
		.orElse(null);
	String maxByLambda = mutableList
		.stream()
		.collect(Collectors.maxBy(Comparator.comparing((s) -> s.length())))
		.orElse(null);

	// ImmutableList
	Integer minByImmutable = immutableList
		.stream()
		.collect(Collectors.minBy(Comparator.comparing((n) -> n)))
		.orElse(null);
	Integer maxByImmutable = immutableList
		.stream()
		.collect(Collectors.maxBy(Comparator.comparing((n) -> n)))
		.orElse(null);

	// MutableSet
	String minBySet = mutableSet
		.stream()
		.collect(Collectors.minBy(Comparator.comparing(String::length)))
		.orElse(null);
	String maxBySet = mutableSet
		.stream()
		.collect(Collectors.maxBy(Comparator.comparing(String::length)))
		.orElse(null);
}
