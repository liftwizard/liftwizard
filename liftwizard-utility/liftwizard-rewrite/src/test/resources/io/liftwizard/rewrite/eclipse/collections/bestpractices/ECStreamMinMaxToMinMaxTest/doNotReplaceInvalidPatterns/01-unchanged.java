import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.eclipse.collections.api.list.MutableList;

class Test {
	Optional<String> withIntermediateOperations(MutableList<String> list) {
		return list.stream()
			.filter(s -> s.length() > 3)
			.min(Comparator.naturalOrder());
	}

	Optional<String> jcfListMin(List<String> list) {
		return list.stream().min(Comparator.naturalOrder());
	}

	Optional<String> jcfListMax(List<String> list) {
		return list.stream().max(Comparator.naturalOrder());
	}
}
