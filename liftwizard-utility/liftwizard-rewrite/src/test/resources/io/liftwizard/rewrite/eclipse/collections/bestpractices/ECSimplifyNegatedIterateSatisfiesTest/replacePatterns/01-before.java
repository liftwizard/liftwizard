import java.util.List;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.impl.block.factory.Predicates;
import org.eclipse.collections.impl.utility.Iterate;

class Test
{
	void test(List<String> list, Predicate<String> predicate1, Predicate<String> predicate2)
	{
		boolean negatedNoneSatisfy = !Iterate.noneSatisfy(list, predicate1);
		boolean negatedAnySatisfy = !Iterate.anySatisfy(list, predicate2);
		boolean negatedWithParentheses = !Iterate.noneSatisfy(list, predicate1);
		boolean negatedWithLambda = !Iterate.noneSatisfy(list, (str) -> str.length() > 5);
		boolean negatedWithMethodReference = !Iterate.anySatisfy(list, String::isEmpty);

		boolean noneSatisfyPredicatesNot = Iterate.noneSatisfy(list, Predicates.not(predicate1));
		boolean anySatisfyPredicatesNot = Iterate.anySatisfy(list, Predicates.not(predicate2));
		boolean noneSatisfyPredicatesNotLambda = Iterate.noneSatisfy(list, Predicates.not((str) -> str.length() > 5));
		boolean anySatisfyPredicatesNotMethodRef = Iterate.anySatisfy(list, Predicates.not(String::isEmpty));

		boolean doubleNegationAnySatisfy = !Iterate.anySatisfy(list, Predicates.not(predicate1));
		boolean doubleNegationNoneSatisfy = !Iterate.noneSatisfy(list, Predicates.not(predicate2));
		boolean doubleNegationAnySatisfyLambda = !Iterate.anySatisfy(list, Predicates.not((str) -> str.length() > 5));
		boolean doubleNegationNoneSatisfyMethodRef = !Iterate.noneSatisfy(list, Predicates.not(String::isEmpty));

		if (!Iterate.noneSatisfy(list, predicate1) && !Iterate.anySatisfy(list, predicate2))
		{
			// Both conditions met
		}

		if (
			Iterate.noneSatisfy(list, Predicates.not(predicate1))
			|| Iterate.anySatisfy(list, Predicates.not(predicate2))
		)
		{
			// Either condition met
		}

		if (
			!Iterate.anySatisfy(list, Predicates.not(predicate1))
			&& !Iterate.noneSatisfy(list, Predicates.not(predicate2))
		)
		{
			// Double negation in conditions
		}
	}
}
