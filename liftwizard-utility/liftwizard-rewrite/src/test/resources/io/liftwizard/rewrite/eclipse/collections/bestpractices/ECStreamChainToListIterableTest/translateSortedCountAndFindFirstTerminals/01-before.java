import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	void test(MutableList<String> list)
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
}
