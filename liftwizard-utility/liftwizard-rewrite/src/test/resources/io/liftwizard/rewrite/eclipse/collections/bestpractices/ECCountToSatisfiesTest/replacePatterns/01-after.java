import org.eclipse.collections.api.list.MutableList;

class Test
{
	void test(MutableList<String> list)
	{
		boolean countEqualsZero = list.noneSatisfy((s) -> s.length() > 5);
		boolean countGreaterThanZero = list.anySatisfy((s) -> s.length() > 5);
		boolean countNotEqualsZero = list.anySatisfy((s) -> s.length() > 5);
		boolean countLessThanOrEqualZero = list.noneSatisfy((s) -> s.length() > 5);
		boolean countGreaterThanOrEqualOne = list.anySatisfy((s) -> s.length() > 5);
		boolean reversedZeroGreaterThanOrEqualCount = list.noneSatisfy((s) -> s.length() > 5);
		boolean reversedOneLessThanOrEqualCount = list.anySatisfy((s) -> s.length() > 5);
		boolean reversedZeroLessThanCount = list.anySatisfy((s) -> s.length() > 5);

		boolean otherComparison = list.count((s) -> s.length() > 5) >= 2;

		if (list.noneSatisfy((s) -> s.length() > 5))
		{
			// None satisfy
		}
	}
}
