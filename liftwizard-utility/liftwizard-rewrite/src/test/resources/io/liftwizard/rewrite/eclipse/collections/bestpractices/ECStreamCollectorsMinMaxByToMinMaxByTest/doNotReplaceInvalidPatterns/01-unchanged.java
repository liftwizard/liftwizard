import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	MutableList<String> mutableList;
	List<String> jcfList;
	Comparator<String> rawComparator;

	// orElse with non-null default value
	String invalidMin1 = mutableList
		.stream()
		.collect(Collectors.minBy(Comparator.comparing(String::length)))
		.orElse("default");
	String invalidMax1 = mutableList
		.stream()
		.collect(Collectors.maxBy(Comparator.comparing(String::length)))
		.orElse("default");

	// Non-Eclipse Collections type (JCF List)
	String invalidMin2 = jcfList
		.stream()
		.collect(Collectors.minBy(Comparator.comparing(String::length)))
		.orElse(null);
	String invalidMax2 = jcfList
		.stream()
		.collect(Collectors.maxBy(Comparator.comparing(String::length)))
		.orElse(null);

	// Without Comparator.comparing (raw Comparator)
	String invalidMin3 = mutableList.stream().collect(Collectors.minBy(rawComparator)).orElse(null);
	String invalidMax3 = mutableList.stream().collect(Collectors.maxBy(rawComparator)).orElse(null);

	// Intermediate operations (filter) - methods needed for complex expressions
	String intermediateMin()
	{
		return mutableList
			.stream()
			.filter((s) -> s.length() > 3)
			.collect(Collectors.minBy(Comparator.comparing(String::length)))
			.orElse(null);
	}

	String intermediateMax()
	{
		return mutableList
			.stream()
			.filter((s) -> s.length() > 3)
			.collect(Collectors.maxBy(Comparator.comparing(String::length)))
			.orElse(null);
	}
}
