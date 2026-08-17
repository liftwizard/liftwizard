import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	void withNegatedLambda(MutableList<String> list)
	{
		boolean result = list.noneSatisfy((s) -> s.isEmpty());
	}

	void withNegatedMethodCall(MutableList<String> list)
	{
		boolean lengthCheck = list.noneSatisfy((s) -> s.length() > 5);
		boolean contains = list.noneSatisfy((s) -> s.contains("x"));
	}

	void withNegatedComparison(MutableList<Integer> list)
	{
		boolean result = list.noneSatisfy((n) -> n > 10);
		boolean equality = list.noneSatisfy((n) -> n == 0);
	}

	void inIfCondition(MutableList<String> list)
	{
		if (list.noneSatisfy((s) -> s.isEmpty()))
		{
			this.doWork();
		}
	}

	void withRichIterable(RichIterable<String> iterable)
	{
		boolean result = iterable.noneSatisfy((s) -> s.isEmpty());
	}

	void doWork()
	{
	}
}
