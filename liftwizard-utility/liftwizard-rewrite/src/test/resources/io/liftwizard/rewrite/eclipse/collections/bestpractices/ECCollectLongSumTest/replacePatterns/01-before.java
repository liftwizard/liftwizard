import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	long withCast(MutableList<String> list)
	{
		return list.collectLong((s) -> (long) s.length()).sum();
	}

	long withLambda(MutableList<Long> list)
	{
		return list.collectLong((l) -> l * 2).sum();
	}

	long withRichIterable(RichIterable<Long> iterable)
	{
		return iterable.collectLong((l) -> l * 2).sum();
	}

	void inExpression(MutableList<Long> list)
	{
		long result = list.collectLong((l) -> l).sum() + 10;
		if (list.collectLong((l) -> l * 2).sum() > 100)
		{
			this.doWork();
		}
	}

	void doWork()
	{
	}
}
