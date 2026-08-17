import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

class Test
{
	MutableList<String> mutableList;
	ImmutableList<String> immutableList;
	MutableSet<String> mutableSet;

	double mapToDoubleSumWithMethodReference = mutableList.collectDouble(String::length).sum();

	double mapToDoubleSumWithLambda = mutableList
		.collectDouble((s) -> s.length() * 1.0)
		.sum();

	int mapToIntSum = mutableList.collectInt(String::length).sum();

	long mapToLongSum = mutableList
		.collectLong((s) -> (long) s.length())
		.sum();

	double withImmutableList = immutableList.collectDouble(String::length).sum();

	int withMutableSet = mutableSet.collectInt(String::length).sum();
}
