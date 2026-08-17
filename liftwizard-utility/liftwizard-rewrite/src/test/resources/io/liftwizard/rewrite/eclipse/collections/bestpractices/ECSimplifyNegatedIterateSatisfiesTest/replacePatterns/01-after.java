import java.util.List;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.impl.utility.Iterate;

class Test
{
	void test(List<String> list, Predicate<String> predicate1, Predicate<String> predicate2)
	{
		boolean negatedNoneSatisfy = Iterate.anySatisfy(list, predicate1);
		boolean negatedAnySatisfy = Iterate.noneSatisfy(list, predicate2);
		boolean negatedWithParentheses = Iterate.anySatisfy(list, predicate1);
		boolean negatedWithLambda = Iterate.anySatisfy(list, (str) -> str.length() > 5);
		boolean negatedWithMethodReference = Iterate.noneSatisfy(list, String::isEmpty);

		boolean noneSatisfyPredicatesNot = Iterate.anySatisfy(list, predicate1);
		boolean anySatisfyPredicatesNot = Iterate.noneSatisfy(list, predicate2);
		boolean noneSatisfyPredicatesNotLambda = Iterate.anySatisfy(list, (str) -> str.length() > 5);
		boolean anySatisfyPredicatesNotMethodRef = Iterate.noneSatisfy(list, String::isEmpty);

		boolean doubleNegationAnySatisfy = Iterate.anySatisfy(list, predicate1);
		boolean doubleNegationNoneSatisfy = Iterate.noneSatisfy(list, predicate2);
		boolean doubleNegationAnySatisfyLambda = Iterate.anySatisfy(list, (str) -> str.length() > 5);
		boolean doubleNegationNoneSatisfyMethodRef = Iterate.noneSatisfy(list, String::isEmpty);

		if (Iterate.anySatisfy(list, predicate1) && Iterate.noneSatisfy(list, predicate2))
		{
			// Both conditions met
		}

		if (
			Iterate.anySatisfy(list, predicate1)
			|| Iterate.noneSatisfy(list, predicate2)
		)
		{
			// Either condition met
		}

		if (
			Iterate.anySatisfy(list, predicate1)
			&& Iterate.noneSatisfy(list, predicate2)
		)
		{
			// Double negation in conditions
		}
	}
}
