import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	double withCast(MutableList<String> list)
	{
		return list.sumOfDouble((s) -> (double) s.length());
	}

	double withLambda(MutableList<Double> list)
	{
		return list.sumOfDouble((d) -> d * 2);
	}

	double withRichIterable(RichIterable<Double> iterable)
	{
		return iterable.sumOfDouble((d) -> d * 2);
	}

	void inExpression(MutableList<Double> list)
	{
		double result = list.sumOfDouble((d) -> d) + 10.0;
		if (list.sumOfDouble((d) -> d * 2) > 100.0)
		{
			this.doWork();
		}
	}

	void doWork()
	{
	}
}
