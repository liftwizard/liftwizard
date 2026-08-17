import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	void test(MutableList<String> stringList, MutableList<Integer> intList, RichIterable<String> iterable)
	{
		// select with negated lambda -> reject
		MutableList<String> negatedLambda = stringList.reject((s) -> s.isEmpty());

		// select with != comparison -> reject with ==
		MutableList<Integer> notEqual = intList.reject((n) -> n == 0);

		// reject with negated lambda -> select
		MutableList<String> rejectNegated = stringList.select((s) -> s.isEmpty());

		// reject with != comparison -> select with ==
		MutableList<Integer> rejectNotEqual = intList.select((n) -> n == 0);

		// select with negated method calls
		MutableList<String> lengthCheck = stringList.reject((s) -> s.length() > 5);
		MutableList<String> contains = stringList.reject((s) -> s.contains("x"));

		// works with RichIterable type
		RichIterable<String> richResult = iterable.reject((s) -> s.isEmpty());

		// works in chained calls
		MutableList<String> chained = stringList.reject((s) -> s.isEmpty()).select((s) -> s.startsWith("a"));

		// works in if condition
		if (stringList.reject((s) -> s.isEmpty()).notEmpty())
		{
			this.doWork();
		}
	}

	void doWork()
	{
	}
}
