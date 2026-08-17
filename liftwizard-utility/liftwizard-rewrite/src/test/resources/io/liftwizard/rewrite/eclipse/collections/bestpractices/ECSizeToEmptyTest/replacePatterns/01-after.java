import org.eclipse.collections.api.list.MutableList;

class Test
{
	void test(MutableList<String> list)
	{
		boolean sizeEqualsZero = list.isEmpty();
		boolean sizeGreaterThanZero = list.notEmpty();
		boolean sizeNotEqualsZero = list.notEmpty();
		boolean sizeGreaterThanOrEqualOne = list.notEmpty();
		boolean sizeLessThanOne = list.isEmpty();
		boolean sizeLessThanOrEqualZero = list.isEmpty();
		boolean reversedOneGreaterThanSize = list.isEmpty();
		boolean reversedZeroGreaterThanOrEqualSize = list.isEmpty();
		boolean reversedZeroEqualsSize = list.isEmpty();

		if (list.isEmpty())
		{
			doWork();
		}
	}

	void doWork()
	{
	}
}
