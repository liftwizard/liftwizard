import org.eclipse.collections.api.list.MutableList;

class Test
{
	void test(MutableList<String> list)
	{
		boolean allMatch = list.count((s) -> s.length() > 5) == list.size();
		boolean reversedAllMatch = list.size() == list.count((s) -> s.length() > 5);

		if (list.count((s) -> s.length() > 5) == list.size())
		{
			// All satisfy
		}
	}
}
