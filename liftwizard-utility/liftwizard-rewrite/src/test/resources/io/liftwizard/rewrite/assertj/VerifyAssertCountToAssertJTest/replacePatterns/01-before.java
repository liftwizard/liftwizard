import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.test.Verify;

class Test
{
	void test()
	{
		MutableList<Integer> numbers = Lists.mutable.with(1, 2, 3, 4, 5);
		Verify.assertCount(2, numbers, (each) -> each % 2 == 0);

		MutableList<String> emptyList = Lists.mutable.empty();
		Verify.assertCount(0, emptyList, (s) -> s.length() > 0);
	}
}
