import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.block.factory.Predicates;

class Test
{
	void test(MutableList<String> list, Predicate<String> predicate)
	{
		boolean noneSatisfyPredicatesNot = list.noneSatisfy(Predicates.not(predicate));
		boolean noneSatisfyPredicatesNotLambda = list.noneSatisfy(Predicates.not((s) -> s.length() > 5));
		boolean noneSatisfyPredicatesNotMethodRef = list.noneSatisfy(Predicates.not(String::isEmpty));

		if (list.noneSatisfy(Predicates.not((s) -> s.isEmpty())))
		{
			this.doWork();
		}
	}

	void doWork()
	{
	}
}
