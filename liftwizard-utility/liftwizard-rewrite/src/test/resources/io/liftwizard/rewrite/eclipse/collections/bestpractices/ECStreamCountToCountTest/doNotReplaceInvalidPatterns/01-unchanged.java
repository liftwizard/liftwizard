import java.util.stream.Stream;

import org.eclipse.collections.api.list.MutableList;

class Test {
	long withMultipleIntermediateOperations(MutableList<String> list) {
		return list.stream()
			.filter(s -> s.length() > 3)
			.map(String::toUpperCase)
			.count();
	}

	Stream<String> onlyStream(MutableList<String> list) {
		return list.stream();
	}

	long onlyCount(MutableList<String> list) {
		return list.stream().count();
	}
}
