import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.collections.api.list.MutableList;

class Test {
	MutableList<String> mutableList;
	ArrayList<String> arrayList;
	Comparator<String> rawComparator;

	// Non-Eclipse Collections type (JCF ArrayList)
	List<String> invalid1 = arrayList.stream().sorted(Comparator.comparing(String::length)).collect(Collectors.toList());

	// Without Comparator.comparing (raw Comparator)
	List<String> invalid2 = mutableList.stream().sorted(rawComparator).collect(Collectors.toList());

	// Intermediate operations (filter before sorted)
	List<String> invalid3() {
		return mutableList.stream()
			.filter(s -> s.length() > 3)
			.sorted(Comparator.comparing(String::length))
			.collect(Collectors.toList());
	}

	// toUnmodifiableList instead of toList
	List<String> invalid4 = mutableList.stream().sorted(Comparator.comparing(String::length)).collect(Collectors.toUnmodifiableList());
}
