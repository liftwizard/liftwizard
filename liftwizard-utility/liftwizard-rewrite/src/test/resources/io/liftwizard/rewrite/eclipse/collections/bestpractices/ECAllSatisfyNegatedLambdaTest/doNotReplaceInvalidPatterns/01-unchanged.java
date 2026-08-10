import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.list.MutableList;

class Test {
	void nonNegatedLambda(MutableList<String> list) {
		boolean result = list.allSatisfy(s -> s.isEmpty());
		boolean lengthCheck = list.allSatisfy(s -> s.length() > 5);
	}

	void methodReference(MutableList<String> list, Predicate<String> predicate) {
		boolean result = list.allSatisfy(String::isEmpty);
		boolean withPredicate = list.allSatisfy(predicate);
	}

	void anySatisfyOrNoneSatisfy(MutableList<String> list) {
		boolean any = list.anySatisfy(s -> !s.isEmpty());
		boolean none = list.noneSatisfy(s -> !s.isEmpty());
	}
}
