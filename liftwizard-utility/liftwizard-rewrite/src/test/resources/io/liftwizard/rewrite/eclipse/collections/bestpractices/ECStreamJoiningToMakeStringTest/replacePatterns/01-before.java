import java.util.stream.Collectors;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

class Test
{
	MutableList<String> stringList;
	MutableList<Integer> intList;
	ImmutableList<String> immutableList;
	MutableSet<String> mutableSet;

	// Basic joining with MutableList<String>
	String result1 = stringList.stream().collect(Collectors.joining(", "));

	// stream().map(Object::toString).collect(joining)
	String result2 = intList.stream().map(Object::toString).collect(Collectors.joining(", "));

	// Empty delimiter
	String result3 = stringList.stream().collect(Collectors.joining(""));

	// ImmutableList
	String result4 = immutableList.stream().collect(Collectors.joining("-"));

	// MutableSet
	String result5 = mutableSet.stream().collect(Collectors.joining("|"));

	// stream().map(String::toString).collect(joining)
	String result6 = stringList.stream().map(String::toString).collect(Collectors.joining(", "));
}
