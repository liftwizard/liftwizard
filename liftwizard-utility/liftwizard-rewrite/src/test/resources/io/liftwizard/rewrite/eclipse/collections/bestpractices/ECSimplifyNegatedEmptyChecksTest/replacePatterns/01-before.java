import org.eclipse.collections.api.list.MutableList;

class Test
{
	boolean testNegatedIsEmpty(MutableList<String> list)
	{
		return !list.isEmpty();
	}

	boolean testNegatedNotEmpty(MutableList<String> list)
	{
		return !list.notEmpty();
	}

	void testInIfStatement(MutableList<String> list)
	{
		if (!list.isEmpty())
		{
			this.doWork();
		}
	}

	void doWork()
	{
	}
}
