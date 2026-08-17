import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	void test(MutableList<String> list)
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
}
