import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

class Test
{
	MutableList<String> mutableList;
	ImmutableList<String> immutableList;
	MutableSet<String> mutableSet;

	double mapToDoubleSumWithMethodReference = mutableList.stream().mapToDouble(String::length).sum();

	double mapToDoubleSumWithLambda = mutableList
		.stream()
		.mapToDouble((s) -> s.length() * 1.0)
		.sum();

	int mapToIntSum = mutableList.stream().mapToInt(String::length).sum();

	long mapToLongSum = mutableList
		.stream()
		.mapToLong((s) -> (long) s.length())
		.sum();

	double withImmutableList = immutableList.stream().mapToDouble(String::length).sum();

	int withMutableSet = mutableSet.stream().mapToInt(String::length).sum();
}
