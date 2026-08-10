import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.collections.api.list.MutableList;

class Test {
	MutableList<String> mutableList;
	ArrayList<String> arrayList;

	// Collectors.toList() - different recipe handles mutable toList
	List<String> invalid1 = mutableList.stream().collect(Collectors.toList());

	// Collectors.toSet() - different recipe handles mutable toSet
	Set<String> invalid2 = mutableList.stream().collect(Collectors.toSet());

	// Only stream without collect
	Stream<String> invalid3 = mutableList.stream();

	// Non-Eclipse Collections type
	List<String> invalid4 = arrayList.stream().collect(Collectors.toUnmodifiableList());

	// Intermediate operations (filter)
	List<String> invalid5() {
		return mutableList.stream()
			.filter(s -> s.length() > 3)
			.collect(Collectors.toUnmodifiableList());
	}
}
