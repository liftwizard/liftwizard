import java.util.Comparator;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	MutableList<String> sortThisSelect(MutableList<String> list)
	{
		return list.sortThis().select((s) -> s.length() > 3);
	}

	MutableList<String> sortThisWithComparatorSelect(MutableList<String> list)
	{
		return list.sortThis(Comparator.comparing(String::length)).select((s) -> s.length() > 3);
	}

	MutableList<String> sortThisReject(MutableList<String> list)
	{
		return list.sortThis().reject((s) -> s.isEmpty());
	}

	MutableList<String> sortThisWithComparatorReject(MutableList<String> list)
	{
		return list.sortThis(Comparator.reverseOrder()).reject(String::isEmpty);
	}

	void multiplePatterns(MutableList<String> list1, MutableList<Integer> list2)
	{
		MutableList<String> result1 = list1.sortThis().select((s) -> s.length() > 3);
		MutableList<Integer> result2 = list2.sortThis(Comparator.naturalOrder()).reject((i) -> i < 0);
	}
}
