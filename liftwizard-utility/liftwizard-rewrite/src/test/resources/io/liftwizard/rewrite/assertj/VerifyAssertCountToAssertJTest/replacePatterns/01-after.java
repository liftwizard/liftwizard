import org.assertj.core.api.Assertions;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;

class Test
{
	void test()
	{
		MutableList<Integer> numbers = Lists.mutable.with(1, 2, 3, 4, 5);
		Assertions.assertThat(numbers).filteredOn((each) -> each % 2 == 0).hasSize(2);

		MutableList<String> emptyList = Lists.mutable.empty();
		Assertions.assertThat(emptyList).filteredOn((s) -> s.length() > 0).hasSize(0);
	}
}
