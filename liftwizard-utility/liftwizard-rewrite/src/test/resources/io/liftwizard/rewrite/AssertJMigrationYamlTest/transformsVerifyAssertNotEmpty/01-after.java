import java.util.HashMap;
import java.util.Map;

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;

import static org.assertj.core.api.Assertions.assertThat;

class Test
{
	void test()
	{
		MutableList<String> list = Lists.mutable.with("a", "b");
		assertThat(list).as("list should not be empty").isNotEmpty();
		assertThat(list).isNotEmpty();

		Map<String, Integer> map = new HashMap<>();
		map.put("key", 1);
		assertThat(map).as("map should not be empty").isNotEmpty();
		assertThat(map).isNotEmpty();
	}
}
