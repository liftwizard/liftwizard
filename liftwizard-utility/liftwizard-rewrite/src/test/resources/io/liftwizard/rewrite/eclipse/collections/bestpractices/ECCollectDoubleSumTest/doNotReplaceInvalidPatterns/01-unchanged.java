import org.eclipse.collections.api.DoubleIterable;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	double withIntermediateOperations(MutableList<Double> list)
	{
		return list
			.collectDouble((d) -> d)
			.select((i) -> i > 5.0)
			.sum();
	}

	DoubleIterable onlyCollectDouble(MutableList<Double> list)
	{
		return list.collectDouble((d) -> d);
	}
}
