import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.test.Verify;

class Test
{
	void test()
	{
		MutableList<String> list = Lists.mutable.with("a", "b", "c");
		Verify.assertSize(3, list);
		Verify.assertNotEmpty(list);
	}
}
