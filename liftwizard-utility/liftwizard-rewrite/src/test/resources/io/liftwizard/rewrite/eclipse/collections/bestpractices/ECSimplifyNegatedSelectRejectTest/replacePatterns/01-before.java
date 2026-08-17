import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	void test(MutableList<String> stringList, MutableList<Integer> intList, RichIterable<String> iterable)
	{
		// select with negated lambda -> reject
		MutableList<String> negatedLambda = stringList.select((s) -> !s.isEmpty());

		// select with != comparison -> reject with ==
		MutableList<Integer> notEqual = intList.select((n) -> n != 0);

		// reject with negated lambda -> select
		MutableList<String> rejectNegated = stringList.reject((s) -> !s.isEmpty());

		// reject with != comparison -> select with ==
		MutableList<Integer> rejectNotEqual = intList.reject((n) -> n != 0);

		// select with negated method calls
		MutableList<String> lengthCheck = stringList.select((s) -> !(s.length() > 5));
		MutableList<String> contains = stringList.select((s) -> !s.contains("x"));

		// works with RichIterable type
		RichIterable<String> richResult = iterable.select((s) -> !s.isEmpty());

		// works in chained calls
		MutableList<String> chained = stringList.select((s) -> !s.isEmpty()).reject((s) -> !s.startsWith("a"));

		// works in if condition
		if (stringList.select((s) -> !s.isEmpty()).notEmpty())
		{
			this.doWork();
		}
	}

	void doWork()
	{
	}
}
