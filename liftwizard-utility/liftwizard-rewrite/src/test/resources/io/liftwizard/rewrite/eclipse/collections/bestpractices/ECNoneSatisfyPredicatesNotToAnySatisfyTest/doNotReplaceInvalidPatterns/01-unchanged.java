import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.block.factory.Predicates;
import org.eclipse.collections.api.block.predicate.Predicate;

class Test {
	void test(MutableList<String> list, Predicate<String> predicate) {
		boolean nonNegatedNoneSatisfy = list.noneSatisfy(predicate);
		boolean withOtherPredicateMethod = list.noneSatisfy(Predicates.alwaysTrue());
	}
}
