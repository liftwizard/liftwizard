import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.set.MutableSet;

class TestSet
{
	boolean test(MutableSet<Integer> set, Predicate<Integer> predicate)
	{
		return set.noneSatisfy(predicate);
	}
}
