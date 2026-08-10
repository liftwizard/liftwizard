import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.collections.api.list.MutableList;

class Test {
	Optional<String> withMultipleIntermediateOperations(MutableList<String> list) {
		return list.stream()
			.filter(s -> s.length() > 3)
			.map(String::toUpperCase)
			.findFirst();
	}

	Stream<String> onlyStream(MutableList<String> list) {
		return list.stream();
	}

	Optional<String> onlyFindFirst(MutableList<String> list) {
		return list.stream().findFirst();
	}
}
