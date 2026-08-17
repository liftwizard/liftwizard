import org.eclipse.collections.api.list.MutableList;

class Test
{
	void test(MutableList<String> list)
	{
		boolean negatedNoneSatisfy = !list.noneSatisfy((s) -> s.length() > 5);
		boolean negatedAnySatisfy = !list.anySatisfy((s) -> s.length() > 5);
		boolean negatedNoneSatisfyMethodRef = !list.noneSatisfy(String::isEmpty);
		boolean negatedAnySatisfyMethodRef = !list.anySatisfy(String::isEmpty);

		if (!list.noneSatisfy((s) -> s.isEmpty()))
		{
			this.doWork();
		}

		if (!list.anySatisfy((s) -> s.startsWith("a")))
		{
			this.doWork();
		}
	}

	void doWork()
	{
	}
}
