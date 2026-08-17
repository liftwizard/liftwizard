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
	String result1 = mutableList.detect(predicate);

	// Lambda predicate
	String result2 = mutableList
		.detect((s) -> s.length() > 5);

	// Method reference
	String result3 = mutableList.detect(String::isEmpty);

	// ImmutableList
	String result4 = immutableList.detect(predicate);

	// MutableSet
	Integer result5 = mutableSet.detect(intPredicate);
}
