import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	void test(MutableList<String> list, Predicate<String> predicate)
	{
		boolean allSatisfyPredicatesNot = list.noneSatisfy(predicate);
		boolean allSatisfyPredicatesNotLambda = list.noneSatisfy((s) -> s.length() > 5);
		boolean allSatisfyPredicatesNotMethodRef = list.noneSatisfy(String::isEmpty);

		if (list.noneSatisfy((s) -> s.isEmpty()))
		{
			this.doWork();
		}
	}

	void doWork()
	{
	}
}
