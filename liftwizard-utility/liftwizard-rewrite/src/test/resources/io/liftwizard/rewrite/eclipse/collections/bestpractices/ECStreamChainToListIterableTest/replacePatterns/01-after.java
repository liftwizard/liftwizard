import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	void streamChains(MutableList<String> list)
	{
		var result1 = list.toList();
		var result2 = list.drop(1).toArray();
		var result3 = list.take(2);
		var result4 = list
			.select((each) -> !each.isEmpty());
		var result5 = list.collect(String::trim);
		var result6 = list.distinct();
		var result7 = list
			.select((each) -> !each.isEmpty())
			.collect(String::trim)
			.toArray();
	}

	void matchTerminals(MutableList<String> list)
	{
		boolean result1 = list.anySatisfy(String::isEmpty);
		boolean result2 = list.allSatisfy((each) -> each.length() > 1);
		boolean result3 = list.noneSatisfy(String::isBlank);
		boolean result4 = list
			.select((each) -> !each.isEmpty())
			.anySatisfy((each) -> each.length() > 3);
		list.forEach(System.out::println);
	}

	void collectorTerminals(MutableList<String> list)
	{
		List<String> result1 = list.toList();
		Set<String> result2 = list.collect(String::trim).toSet();
		var result3 = list
			.select((each) -> !each.isEmpty())
			.toImmutableList();
		var result4 = list.collect(String::trim).toImmutableSet();
	}

	void sortedCountAndFindFirstTerminals(MutableList<String> list)
	{
		List<String> result1 = list.toSortedList();
		List<String> result2 = list.toSortedList(Comparator.naturalOrder());
		boolean result3 = list.size() > 2;
		boolean result4 =
			2
			< list
				.select((each) -> !each.isEmpty())
				.size();
		Optional<String> result5 = list.detectOptional(String::isEmpty);
		String result6 = list.detectOptional(String::isEmpty).orElse("fallback");
	}

	String joining(MutableList<String> list)
	{
		return list.makeString(", ");
	}
}
