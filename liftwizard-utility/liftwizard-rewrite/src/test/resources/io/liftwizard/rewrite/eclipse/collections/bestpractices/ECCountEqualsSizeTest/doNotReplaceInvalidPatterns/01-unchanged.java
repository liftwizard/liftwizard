import org.eclipse.collections.api.list.MutableList;

class Test
{
	void test(MutableList<String> list, MutableList<String> otherList)
	{
		// Different collections - should not transform
		boolean differentLists = list.count((s) -> s.length() > 5) == otherList.size();

		// Count used standalone - should not transform
		int countResult = list.count((s) -> s.length() > 5);

		// Count compared to zero - different recipe handles this
		boolean countEqualsZero = list.count((s) -> s.length() > 5) == 0;
	}
}
