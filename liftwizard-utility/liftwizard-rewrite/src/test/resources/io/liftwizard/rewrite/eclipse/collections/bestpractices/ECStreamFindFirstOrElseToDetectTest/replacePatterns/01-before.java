import java.util.function.Predicate;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

class Test
{
	MutableList<String> mutableList;
	ImmutableList<String> immutableList;
	MutableSet<Integer> mutableSet;
	Predicate<String> predicate;
	Predicate<Integer> intPredicate;

	// Predicate variable
	String result1 = mutableList.stream().filter(predicate).findFirst().orElse(null);

	// Lambda predicate
	String result2 = mutableList
		.stream()
		.filter((s) -> s.length() > 5)
		.findFirst()
		.orElse(null);

	// Method reference
	String result3 = mutableList.stream().filter(String::isEmpty).findFirst().orElse(null);

	// ImmutableList
	String result4 = immutableList.stream().filter(predicate).findFirst().orElse(null);

	// MutableSet
	Integer result5 = mutableSet.stream().filter(intPredicate).findFirst().orElse(null);
}
