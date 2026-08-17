import org.eclipse.collections.api.list.MutableList;

class Test
{
	void test(MutableList<String> list)
	{
		boolean sizeEqualsZero = list.size() == 0;
		boolean sizeGreaterThanZero = list.size() > 0;
		boolean sizeNotEqualsZero = list.size() != 0;
		boolean sizeGreaterThanOrEqualOne = list.size() >= 1;
		boolean sizeLessThanOne = list.size() < 1;
		boolean sizeLessThanOrEqualZero = list.size() <= 0;
		boolean reversedOneGreaterThanSize = 1 > list.size();
		boolean reversedZeroGreaterThanOrEqualSize = 0 >= list.size();
		boolean reversedZeroEqualsSize = 0 == list.size();

		if (list.size() == 0)
		{
			doWork();
		}
	}

	void doWork()
	{
	}
}
