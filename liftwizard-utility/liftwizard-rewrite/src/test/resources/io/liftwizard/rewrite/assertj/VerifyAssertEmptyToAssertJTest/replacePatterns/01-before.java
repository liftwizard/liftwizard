import java.util.HashMap;
import java.util.Map;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.test.Verify;

class Test
{
	void test()
	{
		MutableList<String> list = Lists.mutable.empty();
		Verify.assertEmpty("list should be empty", list);
		Verify.assertEmpty(list);

		Map<String, Integer> map = new HashMap<>();
		Verify.assertEmpty("map should be empty", map);
		Verify.assertEmpty(map);
	}
}
