import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;

import static org.assertj.core.api.Assertions.assertThat;

class Test
{
	void test()
	{
		MutableList<String> list = Lists.mutable.with("a", "b", "c");
		assertThat(list).hasSize(3);
		assertThat(list).isNotEmpty();
	}
}
