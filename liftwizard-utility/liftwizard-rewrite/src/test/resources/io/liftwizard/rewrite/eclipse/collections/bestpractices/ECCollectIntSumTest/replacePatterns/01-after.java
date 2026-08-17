import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	long withMethodReference(MutableList<String> list)
	{
		return list.sumOfInt(String::length);
	}

	long withLambda(MutableList<String> list)
	{
		return list.sumOfInt((s) -> s.length());
	}

	long withRichIterable(RichIterable<Integer> iterable)
	{
		return iterable.sumOfInt((i) -> i * 2);
	}

	void inExpression(MutableList<String> list)
	{
		long result = list.sumOfInt(String::length) + 10;
		if (list.sumOfInt((s) -> s.length()) > 100)
		{
			this.doWork();
		}
	}

	void doWork()
	{
	}
}
