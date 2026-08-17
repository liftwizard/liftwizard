import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;

class Test
{
	void test()
	{
		MutableList<String> list = Lists.mutable.empty();
		Assertions.assertThat(list).as("list should be empty").isEmpty();
		Assertions.assertThat(list).isEmpty();

		Map<String, Integer> map = new HashMap<>();
		Assertions.assertThat(map).as("map should be empty").isEmpty();
		Assertions.assertThat(map).isEmpty();
	}
}
