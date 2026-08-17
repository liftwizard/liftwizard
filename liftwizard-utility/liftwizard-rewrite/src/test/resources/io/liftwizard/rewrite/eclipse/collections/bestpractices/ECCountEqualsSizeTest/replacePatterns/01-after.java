import org.eclipse.collections.api.list.MutableList;

class Test
{
	void test(MutableList<String> list)
	{
		boolean allMatch = list.allSatisfy((s) -> s.length() > 5);
		boolean reversedAllMatch = list.allSatisfy((s) -> s.length() > 5);

		if (list.allSatisfy((s) -> s.length() > 5))
		{
			// All satisfy
		}
	}
}
