import java.util.function.Predicate;

import org.eclipse.collections.api.list.MutableList;

class Test {
	void otherPredicates(MutableList<String> list, Predicate<String> predicate) {
		boolean anyMatchPredicate = list.stream().anyMatch(predicate);
		boolean anyMatchLambda = list.stream().anyMatch(s -> s.length() > 5);
		boolean directContains = list.contains("hello");
	}

	boolean withIntermediateOperations(MutableList<String> list, String target) {
		return list.stream()
			.filter(s -> s.length() > 3)
			.anyMatch(target::equals);
	}
}
