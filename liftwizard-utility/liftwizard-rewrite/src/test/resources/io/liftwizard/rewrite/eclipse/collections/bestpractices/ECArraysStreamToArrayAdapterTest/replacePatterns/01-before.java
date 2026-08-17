import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

class Test
{
	void directTerminals(String[] values, String[] names)
	{
		var result1 = Arrays.stream(values).toList();
		List<String> result2 = Arrays.stream(names).toList();
		var result3 = Arrays.stream(new String[]
		{
			"a",
			"b",
			"c",
		}).toList();
	}

	void collectorTerminals(String[] values)
	{
		List<String> result1 = Arrays.stream(values).collect(Collectors.toList());
		Set<String> result2 = Arrays.stream(values).map(String::trim).collect(Collectors.toSet());
		var result3 = Arrays.stream(values)
			.filter((each) -> !each.isEmpty())
			.collect(Collectors.toUnmodifiableList());
		var result4 = Arrays.stream(values).map(String::trim).collect(Collectors.toUnmodifiableSet());
	}

	void sortedCountAndFindFirstTerminals(String[] values)
	{
		List<String> result1 = Arrays.stream(values).sorted().collect(Collectors.toList());
		List<String> result2 = Arrays.stream(values).sorted(Comparator.naturalOrder()).toList();
		boolean result3 = Arrays.stream(values).count() > 2;
		boolean result4 =
			2
			< Arrays.stream(values)
				.filter((each) -> !each.isEmpty())
				.count();
		Optional<String> result5 = Arrays.stream(values).filter(String::isEmpty).findFirst();
		String result6 = Arrays.stream(values).filter(String::isEmpty).findFirst().orElse("fallback");
	}

	void streamChains(String[] values)
	{
		var result1 = Arrays.stream(values).skip(1).toArray();
		var result2 = Arrays.stream(values).limit(2).toList();
		var result3 = Arrays.stream(values)
			.filter((each) -> !each.isEmpty())
			.toList();
		var result4 = Arrays.stream(values).map(String::trim).toList();
		var result5 = Arrays.stream(values).distinct().toList();
		var result6 = Arrays.stream(values)
			.filter((each) -> !each.isEmpty())
			.map(String::trim)
			.toArray();
	}

	void matchTerminals(String[] values)
	{
		boolean result1 = Arrays.stream(values).anyMatch(String::isEmpty);
		boolean result2 = Arrays.stream(values).allMatch((each) -> each.length() > 1);
		boolean result3 = Arrays.stream(values).noneMatch(String::isBlank);
		boolean result4 = Arrays.stream(values)
			.filter((each) -> !each.isEmpty())
			.anyMatch((each) -> each.length() > 3);
	}

	void safeTerminalConsumers(String[] values)
	{
		var result1 = Arrays.stream(values).toArray();
		var result2 = Arrays.stream(values).iterator();
		Arrays.stream(values).forEach((each) -> each.trim());
		Arrays.stream(values).forEach(System.out::println);
	}
}
