import org.eclipse.collections.api.list.MutableList;

class Test
{
	void test(MutableList<String> list)
	{
		boolean countEqualsZero = list.count((s) -> s.length() > 5) == 0;
		boolean countGreaterThanZero = list.count((s) -> s.length() > 5) > 0;
		boolean countNotEqualsZero = list.count((s) -> s.length() > 5) != 0;
		boolean countLessThanOrEqualZero = list.count((s) -> s.length() > 5) <= 0;
		boolean countGreaterThanOrEqualOne = list.count((s) -> s.length() > 5) >= 1;
		boolean reversedZeroGreaterThanOrEqualCount = 0 >= list.count((s) -> s.length() > 5);
		boolean reversedOneLessThanOrEqualCount = 1 <= list.count((s) -> s.length() > 5);
		boolean reversedZeroLessThanCount = 0 < list.count((s) -> s.length() > 5);

		boolean otherComparison = list.count((s) -> s.length() > 5) >= 2;

		if (list.count((s) -> s.length() > 5) == 0)
		{
			// None satisfy
		}
	}
}
