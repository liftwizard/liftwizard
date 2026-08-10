import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.block.factory.Predicates;
import org.eclipse.collections.api.block.predicate.Predicate;

class Test {
	void test(MutableList<String> list, Predicate<String> predicate) {
		boolean nonNegatedAnySatisfy = list.anySatisfy(predicate);
		boolean withOtherPredicateMethod = list.anySatisfy(Predicates.alwaysTrue());
	}
}
