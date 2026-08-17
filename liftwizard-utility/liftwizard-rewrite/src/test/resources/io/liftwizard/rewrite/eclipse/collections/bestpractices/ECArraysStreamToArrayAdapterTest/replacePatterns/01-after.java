import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.collections.impl.list.fixed.ArrayAdapter;

class Test
{
	void directTerminals(String[] values, String[] names)
	{
		var result1 = ArrayAdapter.adapt(values).toList();
		List<String> result2 = ArrayAdapter.adapt(names).toList();
		var result3 = ArrayAdapter.adapt(new String[]
				{
						"a",
						"b",
						"c",
		}).toList();
	}

	void collectorTerminals(String[] values)
	{
		List<String> result1 = ArrayAdapter.adapt(values).toList();
		Set<String> result2 = ArrayAdapter.adapt(values).collect(String::trim).toSet();
		var result3 = ArrayAdapter.adapt(values)
			.select((each) -> !each.isEmpty())
			.toImmutableList();
		var result4 = ArrayAdapter.adapt(values).collect(String::trim).toImmutableSet();
	}

	void sortedCountAndFindFirstTerminals(String[] values)
	{
		List<String> result1 = ArrayAdapter.adapt(values).toSortedList();
		List<String> result2 = ArrayAdapter.adapt(values).toSortedList(Comparator.naturalOrder());
		boolean result3 = ArrayAdapter.adapt(values).size() > 2;
		boolean result4 =
			2
			< ArrayAdapter.adapt(values)
				.select((each) -> !each.isEmpty())
				.size();
		Optional<String> result5 = ArrayAdapter.adapt(values).detectOptional(String::isEmpty);
		String result6 = ArrayAdapter.adapt(values).detectOptional(String::isEmpty).orElse("fallback");
	}

	void streamChains(String[] values)
	{
		var result1 = ArrayAdapter.adapt(values).drop(1).toArray();
		var result2 = ArrayAdapter.adapt(values).take(2);
		var result3 = ArrayAdapter.adapt(values)
			.select((each) -> !each.isEmpty());
		var result4 = ArrayAdapter.adapt(values).collect(String::trim);
		var result5 = ArrayAdapter.adapt(values).distinct();
		var result6 = ArrayAdapter.adapt(values)
			.select((each) -> !each.isEmpty())
			.collect(String::trim)
			.toArray();
	}

	void matchTerminals(String[] values)
	{
		boolean result1 = ArrayAdapter.adapt(values).anySatisfy(String::isEmpty);
		boolean result2 = ArrayAdapter.adapt(values).allSatisfy((each) -> each.length() > 1);
		boolean result3 = ArrayAdapter.adapt(values).noneSatisfy(String::isBlank);
		boolean result4 = ArrayAdapter.adapt(values)
			.select((each) -> !each.isEmpty())
			.anySatisfy((each) -> each.length() > 3);
	}

	void safeTerminalConsumers(String[] values)
	{
		var result1 = ArrayAdapter.adapt(values).toArray();
		var result2 = ArrayAdapter.adapt(values).iterator();
		ArrayAdapter.adapt(values).forEach((each) -> each.trim());
		ArrayAdapter.adapt(values).forEach(System.out::println);
	}
}
