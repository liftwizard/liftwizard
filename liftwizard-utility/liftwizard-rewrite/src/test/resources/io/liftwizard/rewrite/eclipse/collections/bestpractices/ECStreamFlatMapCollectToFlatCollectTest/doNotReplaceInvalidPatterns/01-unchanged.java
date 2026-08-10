import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.collections.api.list.MutableList;

class Test {
	MutableList<MutableList<String>> mutableList;
	ArrayList<List<String>> arrayList;

	// toUnmodifiableList - unmodifiable != immutable
	List<String> invalid1 = mutableList.stream().flatMap(x -> x.stream()).collect(Collectors.toUnmodifiableList());

	// toUnmodifiableSet - unmodifiable != immutable
	Set<String> invalid2 = mutableList.stream().flatMap(x -> x.stream()).collect(Collectors.toUnmodifiableSet());

	// Only stream
	Stream<MutableList<String>> invalid3 = mutableList.stream();

	// Only flatMap without collect
	Stream<String> invalid4 = mutableList.stream().flatMap(x -> x.stream());

	// Non-Eclipse Collections type
	List<String> invalid5 = arrayList.stream().flatMap(x -> x.stream()).collect(Collectors.toList());

	// Lambda body does not end with .stream()
	List<Integer> invalid6 = mutableList.stream().flatMap(x -> Stream.of(x.size())).collect(Collectors.toList());
}
