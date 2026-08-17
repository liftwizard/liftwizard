import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.block.factory.Predicates;

class Test
{
	void test(MutableList<String> list, Predicate<String> predicate)
	{
		boolean allSatisfyPredicatesNot = list.allSatisfy(Predicates.not(predicate));
		boolean allSatisfyPredicatesNotLambda = list.allSatisfy(Predicates.not((s) -> s.length() > 5));
		boolean allSatisfyPredicatesNotMethodRef = list.allSatisfy(Predicates.not(String::isEmpty));

		if (list.allSatisfy(Predicates.not((s) -> s.isEmpty())))
		{
			this.doWork();
		}
	}

	void doWork()
	{
	}
}
