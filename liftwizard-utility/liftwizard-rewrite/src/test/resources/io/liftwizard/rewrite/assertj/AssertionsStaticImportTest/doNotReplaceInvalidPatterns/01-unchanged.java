import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

class Test
{
	void test()
	{
		List<String> list = new ArrayList<>();
		assertThat(list).isEmpty();
	}
}
