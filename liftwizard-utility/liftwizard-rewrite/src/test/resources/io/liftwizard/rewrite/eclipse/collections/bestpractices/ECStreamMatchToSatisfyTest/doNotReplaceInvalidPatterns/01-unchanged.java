import java.util.stream.Stream;

import org.eclipse.collections.api.list.MutableList;

class Test {
	boolean withIntermediateOperations(MutableList<String> list) {
		return list.stream()
			.filter(s -> s.length() > 3)
			.anyMatch(String::isEmpty);
	}

	Stream<String> onlyStream(MutableList<String> list) {
		return list.stream();
	}
}
