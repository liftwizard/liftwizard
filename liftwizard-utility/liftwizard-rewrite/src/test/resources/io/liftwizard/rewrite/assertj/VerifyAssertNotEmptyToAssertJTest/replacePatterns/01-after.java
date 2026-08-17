import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;

class Test
{
	void test()
	{
		MutableList<String> list = Lists.mutable.with("a", "b");
		Assertions.assertThat(list).as("list should not be empty").isNotEmpty();
		Assertions.assertThat(list).isNotEmpty();

		Map<String, Integer> map = new HashMap<>();
		map.put("key", 1);
		Assertions.assertThat(map).as("map should not be empty").isNotEmpty();
		Assertions.assertThat(map).isNotEmpty();

		String[] array =
		{
			"a",
			"b",
		};
		Assertions.assertThat(array).as("array should not be empty").isNotEmpty();
		Assertions.assertThat(array).isNotEmpty();
	}
}
