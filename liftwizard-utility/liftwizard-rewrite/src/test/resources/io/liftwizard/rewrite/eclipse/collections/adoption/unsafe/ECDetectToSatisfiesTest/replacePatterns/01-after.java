import org.eclipse.collections.api.list.MutableList;

class Test
{
	void testMultiplePatterns(MutableList<String> list)
	{
		boolean detectNotNull = list.anySatisfy((s) -> s.length() > 5);
		boolean detectEqualsNull = list.noneSatisfy((s) -> s.length() > 5);
		boolean nullNotEqualsDetect = list.anySatisfy((s) -> s.length() > 5);
		boolean nullEqualsDetect = list.noneSatisfy((s) -> s.length() > 5);
	}
}
