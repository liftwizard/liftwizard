import java.util.HashMap;
import java.util.Map;

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;

import static org.assertj.core.api.Assertions.assertThat;

class Test
{
	void test()
	{
		MutableList<String> list = Lists.mutable.with("a", "b", "c");
		assertThat(list).as("list should have size 3").hasSize(3);
		assertThat(list).hasSize(3);

		String[] array = new String[]
		{
			"a",
			"b",
			"c",
		};
		assertThat(array).as("array should have size 3").hasSize(3);
		assertThat(array).hasSize(3);

		Map<String, Integer> map = new HashMap<>();
		map.put("a", 1);
		map.put("b", 2);
		assertThat(map).as("map should have size 2").hasSize(2);
		assertThat(map).hasSize(2);
	}
}
