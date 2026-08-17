import org.eclipse.collections.api.list.MutableList;

class Test
{
	boolean testNegatedIsEmpty(MutableList<String> list)
	{
		return list.notEmpty();
	}

	boolean testNegatedNotEmpty(MutableList<String> list)
	{
		return list.isEmpty();
	}

	void testInIfStatement(MutableList<String> list)
	{
		if (list.notEmpty())
		{
			this.doWork();
		}
	}

	void doWork()
	{
	}
}
