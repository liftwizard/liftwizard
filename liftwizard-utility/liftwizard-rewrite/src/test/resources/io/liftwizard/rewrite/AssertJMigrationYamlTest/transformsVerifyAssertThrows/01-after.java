import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Test
{
	void test()
	{
		assertThatThrownBy(() ->
		{
			throw new IllegalArgumentException("error");
		}).isInstanceOf(IllegalArgumentException.class);

		assertThatThrownBy(() ->
		{
			throw new NullPointerException();
		}).isInstanceOf(NullPointerException.class);

		Callable<Object> failingCallable = () ->
		{
			throw new RuntimeException("error");
		};
		assertThatThrownBy(failingCallable::call).isInstanceOf(RuntimeException.class);
	}
}
