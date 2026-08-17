import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	long withMethodReference(MutableList<String> list)
	{
		return list.collectInt(String::length).sum();
	}

	long withLambda(MutableList<String> list)
	{
		return list.collectInt((s) -> s.length()).sum();
	}

	long withRichIterable(RichIterable<Integer> iterable)
	{
		return iterable.collectInt((i) -> i * 2).sum();
	}

	void inExpression(MutableList<String> list)
	{
		long result = list.collectInt(String::length).sum() + 10;
		if (list.collectInt((s) -> s.length()).sum() > 100)
		{
			this.doWork();
		}
	}

	void doWork()
	{
	}
}
