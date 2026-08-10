import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.block.predicate.Predicate;

class Test {
	void test(MutableList<String> list, Predicate<String> predicate) {
		boolean anySatisfyPredicate = list.anySatisfy(predicate);
		boolean anySatisfyLambda = list.anySatisfy(s -> s.length() > 5);
		boolean directContains = list.contains("hello");

	}
}
