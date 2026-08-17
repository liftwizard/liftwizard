import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	void withNegatedLambda(MutableList<String> list)
	{
		boolean result = list.allSatisfy((s) -> !s.isEmpty());
	}

	void withNegatedMethodCall(MutableList<String> list)
	{
		boolean lengthCheck = list.allSatisfy((s) -> !(s.length() > 5));
		boolean contains = list.allSatisfy((s) -> !s.contains("x"));
	}

	void withNegatedComparison(MutableList<Integer> list)
	{
		boolean result = list.allSatisfy((n) -> !(n > 10));
		boolean equality = list.allSatisfy((n) -> !(n == 0));
	}

	void inIfCondition(MutableList<String> list)
	{
		if (list.allSatisfy((s) -> !s.isEmpty()))
		{
			this.doWork();
		}
	}

	void withRichIterable(RichIterable<String> iterable)
	{
		boolean result = iterable.allSatisfy((s) -> !s.isEmpty());
	}

	void doWork()
	{
	}
}
