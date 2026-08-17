import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	void test(MutableList<String> list)
	{
		List<String> result1 = list.stream().collect(Collectors.toList());
		Set<String> result2 = list.stream().map(String::trim).collect(Collectors.toSet());
		var result3 = list
			.stream()
			.filter((each) -> !each.isEmpty())
			.collect(Collectors.toUnmodifiableList());
		var result4 = list.stream().map(String::trim).collect(Collectors.toUnmodifiableSet());
	}
}
