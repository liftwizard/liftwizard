import org.eclipse.collections.api.IntIterable;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	long withIntermediateOperations(MutableList<String> list)
	{
		return list
			.collectInt(String::length)
			.select((i) -> i > 5)
			.sum();
	}

	IntIterable onlyCollectInt(MutableList<String> list)
	{
		return list.collectInt(String::length);
	}
}
