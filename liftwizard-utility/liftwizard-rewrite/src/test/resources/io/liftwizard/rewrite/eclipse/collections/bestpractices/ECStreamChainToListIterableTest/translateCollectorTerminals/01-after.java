import java.util.List;
import java.util.Set;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	void test(MutableList<String> list)
	{
		List<String> result1 = list.toList();
		Set<String> result2 = list.collect(String::trim).toSet();
		var result3 = list
			.select((each) -> !each.isEmpty())
			.toImmutableList();
		var result4 = list.collect(String::trim).toImmutableSet();
	}
}
