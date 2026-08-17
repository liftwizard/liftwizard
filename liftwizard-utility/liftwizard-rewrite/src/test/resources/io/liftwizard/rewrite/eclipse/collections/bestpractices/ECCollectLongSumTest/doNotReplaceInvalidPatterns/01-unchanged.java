import org.eclipse.collections.api.LongIterable;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	long withIntermediateOperations(MutableList<Long> list)
	{
		return list
			.collectLong((l) -> l)
			.select((i) -> i > 5)
			.sum();
	}

	LongIterable onlyCollectLong(MutableList<Long> list)
	{
		return list.collectLong((l) -> l);
	}
}
