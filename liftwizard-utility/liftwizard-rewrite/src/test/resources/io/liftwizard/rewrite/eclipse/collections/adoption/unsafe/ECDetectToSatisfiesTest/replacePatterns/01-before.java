import org.eclipse.collections.api.list.MutableList;

class Test
{
	void testMultiplePatterns(MutableList<String> list)
	{
		boolean detectNotNull = list.detect((s) -> s.length() > 5) != null;
		boolean detectEqualsNull = list.detect((s) -> s.length() > 5) == null;
		boolean nullNotEqualsDetect = null != list.detect((s) -> s.length() > 5);
		boolean nullEqualsDetect = null == list.detect((s) -> s.length() > 5);
	}
}
