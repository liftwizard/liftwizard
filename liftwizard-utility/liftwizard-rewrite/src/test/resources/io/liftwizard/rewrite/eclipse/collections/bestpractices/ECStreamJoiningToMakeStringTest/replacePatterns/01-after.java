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
	String result1 = stringList.makeString(", ");

	// stream().map(Object::toString).collect(joining)
	String result2 = intList.makeString(", ");

	// Empty delimiter
	String result3 = stringList.makeString("");

	// ImmutableList
	String result4 = immutableList.makeString("-");

	// MutableSet
	String result5 = mutableSet.makeString("|");

	// stream().map(String::toString).collect(joining)
	String result6 = stringList.makeString(", ");
}
