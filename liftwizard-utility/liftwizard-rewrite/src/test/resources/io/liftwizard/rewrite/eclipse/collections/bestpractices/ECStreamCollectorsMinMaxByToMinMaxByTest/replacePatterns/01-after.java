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
		.minBy(String::length);
	String maxByMethodRef = mutableList
		.maxBy(String::length);

	// Lambda
	String minByLambda = mutableList
		.minBy((s) -> s.length());
	String maxByLambda = mutableList
		.maxBy((s) -> s.length());

	// ImmutableList
	Integer minByImmutable = immutableList
		.minBy((n) -> n);
	Integer maxByImmutable = immutableList
		.maxBy((n) -> n);

	// MutableSet
	String minBySet = mutableSet
		.minBy(String::length);
	String maxBySet = mutableSet
		.maxBy(String::length);
}
