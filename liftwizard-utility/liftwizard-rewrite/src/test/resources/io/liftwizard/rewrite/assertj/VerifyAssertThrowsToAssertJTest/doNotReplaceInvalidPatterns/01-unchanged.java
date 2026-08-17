import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.Callable;

class Test
{
	void test()
	{
		assertThatThrownBy(() ->
		{
			throw new IllegalArgumentException("error");
		}).isInstanceOf(IllegalArgumentException.class);
	}
}
