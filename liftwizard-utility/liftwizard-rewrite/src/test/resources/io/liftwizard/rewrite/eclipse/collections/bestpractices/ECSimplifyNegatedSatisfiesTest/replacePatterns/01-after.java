import org.eclipse.collections.api.list.MutableList;

class Test
{
	void test(MutableList<String> list)
	{
		boolean negatedNoneSatisfy = list.anySatisfy((s) -> s.length() > 5);
		boolean negatedAnySatisfy = list.noneSatisfy((s) -> s.length() > 5);
		boolean negatedNoneSatisfyMethodRef = list.anySatisfy(String::isEmpty);
		boolean negatedAnySatisfyMethodRef = list.noneSatisfy(String::isEmpty);

		if (list.anySatisfy((s) -> s.isEmpty()))
		{
			this.doWork();
		}

		if (list.noneSatisfy((s) -> s.startsWith("a")))
		{
			this.doWork();
		}
	}

	void doWork()
	{
	}
}
