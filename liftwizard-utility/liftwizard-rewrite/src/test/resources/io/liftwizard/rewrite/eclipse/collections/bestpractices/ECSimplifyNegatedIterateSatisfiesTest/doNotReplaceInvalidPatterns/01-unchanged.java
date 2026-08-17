import java.util.List;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.impl.block.factory.Predicates;
import org.eclipse.collections.impl.utility.Iterate;

class Test
{
	void test(List<String> list, Predicate<String> predicate)
	{
		boolean nonNegatedNoneSatisfy = Iterate.noneSatisfy(list, predicate);
		boolean nonNegatedAnySatisfy = Iterate.anySatisfy(list, predicate);
		boolean withOtherPredicateMethod = Iterate.noneSatisfy(list, Predicates.alwaysTrue());
	}
}
