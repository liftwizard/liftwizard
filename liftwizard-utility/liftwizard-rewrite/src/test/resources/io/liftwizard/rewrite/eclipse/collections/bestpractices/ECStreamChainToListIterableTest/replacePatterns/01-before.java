import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	void streamChains(MutableList<String> list)
	{
		var result1 = list.stream().toList();
		var result2 = list.stream().skip(1).toArray();
		var result3 = list.stream().limit(2).toList();
		var result4 = list
			.stream()
			.filter((each) -> !each.isEmpty())
			.toList();
		var result5 = list.stream().map(String::trim).toList();
		var result6 = list.stream().distinct().toList();
		var result7 = list
			.stream()
			.filter((each) -> !each.isEmpty())
			.map(String::trim)
			.toArray();
	}

	void matchTerminals(MutableList<String> list)
	{
		boolean result1 = list.stream().anyMatch(String::isEmpty);
		boolean result2 = list.stream().allMatch((each) -> each.length() > 1);
		boolean result3 = list.stream().noneMatch(String::isBlank);
		boolean result4 = list
			.stream()
			.filter((each) -> !each.isEmpty())
			.anyMatch((each) -> each.length() > 3);
		list.stream().forEach(System.out::println);
	}

	void collectorTerminals(MutableList<String> list)
	{
		List<String> result1 = list.stream().collect(Collectors.toList());
		Set<String> result2 = list.stream().map(String::trim).collect(Collectors.toSet());
		var result3 = list
			.stream()
			.filter((each) -> !each.isEmpty())
			.collect(Collectors.toUnmodifiableList());
		var result4 = list.stream().map(String::trim).collect(Collectors.toUnmodifiableSet());
	}

	void sortedCountAndFindFirstTerminals(MutableList<String> list)
	{
		List<String> result1 = list.stream().sorted().collect(Collectors.toList());
		List<String> result2 = list.stream().sorted(Comparator.naturalOrder()).toList();
		boolean result3 = list.stream().count() > 2;
		boolean result4 =
			2
			< list
				.stream()
				.filter((each) -> !each.isEmpty())
				.count();
		Optional<String> result5 = list.stream().filter(String::isEmpty).findFirst();
		String result6 = list.stream().filter(String::isEmpty).findFirst().orElse("fallback");
	}

	String joining(MutableList<String> list)
	{
		return list.stream().collect(Collectors.joining(", "));
	}
}
