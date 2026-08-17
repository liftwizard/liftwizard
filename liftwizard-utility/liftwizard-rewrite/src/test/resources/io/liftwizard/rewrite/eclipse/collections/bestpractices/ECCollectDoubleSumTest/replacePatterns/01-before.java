import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	double withCast(MutableList<String> list)
	{
		return list.collectDouble((s) -> (double) s.length()).sum();
	}

	double withLambda(MutableList<Double> list)
	{
		return list.collectDouble((d) -> d * 2).sum();
	}

	double withRichIterable(RichIterable<Double> iterable)
	{
		return iterable.collectDouble((d) -> d * 2).sum();
	}

	void inExpression(MutableList<Double> list)
	{
		double result = list.collectDouble((d) -> d).sum() + 10.0;
		if (list.collectDouble((d) -> d * 2).sum() > 100.0)
		{
			this.doWork();
		}
	}

	void doWork()
	{
	}
}
