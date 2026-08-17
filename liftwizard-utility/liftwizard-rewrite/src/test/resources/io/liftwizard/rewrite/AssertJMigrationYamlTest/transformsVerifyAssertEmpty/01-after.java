import java.util.HashMap;
import java.util.Map;

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;

import static org.assertj.core.api.Assertions.assertThat;

class Test
{
	void test()
	{
		MutableList<String> list = Lists.mutable.empty();
		assertThat(list).as("list should be empty").isEmpty();
		assertThat(list).isEmpty();

		Map<String, Integer> map = new HashMap<>();
		assertThat(map).as("map should be empty").isEmpty();
		assertThat(map).isEmpty();
	}
}
