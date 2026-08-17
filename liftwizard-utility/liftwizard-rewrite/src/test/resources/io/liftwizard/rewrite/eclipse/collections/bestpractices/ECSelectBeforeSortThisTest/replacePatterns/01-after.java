import java.util.Comparator;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	MutableList<String> sortThisSelect(MutableList<String> list)
	{
		return list.select((s) -> s.length() > 3).sortThis();
	}

	MutableList<String> sortThisWithComparatorSelect(MutableList<String> list)
	{
		return list.select((s) -> s.length() > 3).sortThis(Comparator.comparing(String::length));
	}

	MutableList<String> sortThisReject(MutableList<String> list)
	{
		return list.reject((s) -> s.isEmpty()).sortThis();
	}

	MutableList<String> sortThisWithComparatorReject(MutableList<String> list)
	{
		return list.reject(String::isEmpty).sortThis(Comparator.reverseOrder());
	}

	void multiplePatterns(MutableList<String> list1, MutableList<Integer> list2)
	{
		MutableList<String> result1 = list1.select((s) -> s.length() > 3).sortThis();
		MutableList<Integer> result2 = list2.reject((i) -> i < 0).sortThis(Comparator.naturalOrder());
	}
}
