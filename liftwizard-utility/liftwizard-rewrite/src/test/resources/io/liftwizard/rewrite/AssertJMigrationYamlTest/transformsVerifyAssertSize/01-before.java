import java.util.HashMap;
import java.util.Map;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.test.Verify;

class Test
{
	void test()
	{
		MutableList<String> list = Lists.mutable.with("a", "b", "c");
		Verify.assertSize("list should have size 3", 3, list);
		Verify.assertSize(3, list);

		String[] array = new String[]
		{
			"a",
			"b",
			"c",
		};
		Verify.assertSize("array should have size 3", 3, array);
		Verify.assertSize(3, array);

		Map<String, Integer> map = new HashMap<>();
		map.put("a", 1);
		map.put("b", 2);
		Verify.assertSize("map should have size 2", 2, map);
		Verify.assertSize(2, map);
	}
}
