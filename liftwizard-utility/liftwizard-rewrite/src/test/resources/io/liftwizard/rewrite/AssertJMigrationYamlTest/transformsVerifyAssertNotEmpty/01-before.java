import java.util.HashMap;
import java.util.Map;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.test.Verify;

class Test
{
	void test()
	{
		MutableList<String> list = Lists.mutable.with("a", "b");
		Verify.assertNotEmpty("list should not be empty", list);
		Verify.assertNotEmpty(list);

		Map<String, Integer> map = new HashMap<>();
		map.put("key", 1);
		Verify.assertNotEmpty("map should not be empty", map);
		Verify.assertNotEmpty(map);
	}
}
