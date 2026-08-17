import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	long withCast(MutableList<String> list)
	{
		return list.sumOfLong((s) -> (long) s.length());
	}

	long withLambda(MutableList<Long> list)
	{
		return list.sumOfLong((l) -> l * 2);
	}

	long withRichIterable(RichIterable<Long> iterable)
	{
		return iterable.sumOfLong((l) -> l * 2);
	}

	void inExpression(MutableList<Long> list)
	{
		long result = list.sumOfLong((l) -> l) + 10;
		if (list.sumOfLong((l) -> l * 2) > 100)
		{
			this.doWork();
		}
	}

	void doWork()
	{
	}
}
