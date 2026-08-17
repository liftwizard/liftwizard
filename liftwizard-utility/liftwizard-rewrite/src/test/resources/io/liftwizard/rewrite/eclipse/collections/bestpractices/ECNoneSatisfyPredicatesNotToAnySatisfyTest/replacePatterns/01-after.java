import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	void test(MutableList<String> list, Predicate<String> predicate)
	{
		boolean noneSatisfyPredicatesNot = list.anySatisfy(predicate);
		boolean noneSatisfyPredicatesNotLambda = list.anySatisfy((s) -> s.length() > 5);
		boolean noneSatisfyPredicatesNotMethodRef = list.anySatisfy(String::isEmpty);

		if (list.anySatisfy((s) -> s.isEmpty()))
		{
			this.doWork();
		}
	}

	void doWork()
	{
	}
}
