import java.util.List;
import java.util.Optional;

import org.eclipse.collections.api.list.MutableList;

class Test {
	Integer withIntermediateOperations(MutableList<Integer> list) {
		return list.stream()
			.filter(i -> i > 0)
			.reduce(0, Integer::sum);
	}

	Optional<Integer> withOptionalReduce(MutableList<Integer> list) {
		return list.stream().reduce(Integer::sum);
	}

	Integer withThreeArgumentReduce(MutableList<String> list) {
		return list.stream().reduce(0, (acc, s) -> acc + s.length(), Integer::sum);
	}

	Integer withJcfList(List<Integer> list) {
		return list.stream().reduce(0, Integer::sum);
	}
}
