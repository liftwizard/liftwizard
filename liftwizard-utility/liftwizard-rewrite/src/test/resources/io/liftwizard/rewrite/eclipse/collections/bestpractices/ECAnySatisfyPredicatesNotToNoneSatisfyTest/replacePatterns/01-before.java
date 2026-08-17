import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.block.factory.Predicates;

class Test
{
	void test(MutableList<String> list, Predicate<String> predicate)
	{
		boolean anySatisfyPredicatesNot = list.anySatisfy(Predicates.not(predicate));
		boolean anySatisfyPredicatesNotLambda = list.anySatisfy(Predicates.not((s) -> s.length() > 5));
		boolean anySatisfyPredicatesNotMethodRef = list.anySatisfy(Predicates.not(String::isEmpty));

		if (list.anySatisfy(Predicates.not((s) -> s.isEmpty())))
		{
			this.doWork();
		}
	}

	void doWork()
	{
	}
}
