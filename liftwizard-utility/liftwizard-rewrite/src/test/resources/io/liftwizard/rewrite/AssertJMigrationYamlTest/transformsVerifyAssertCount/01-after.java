import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;

import static org.assertj.core.api.Assertions.assertThat;

class Test
{
	void test()
	{
		MutableList<Integer> numbers = Lists.mutable.with(1, 2, 3, 4, 5);
		assertThat(numbers).filteredOn((each) -> each % 2 == 0).hasSize(2);

		MutableList<String> emptyList = Lists.mutable.empty();
		assertThat(emptyList).filteredOn((s) -> s.length() > 0).hasSize(0);
	}
}
