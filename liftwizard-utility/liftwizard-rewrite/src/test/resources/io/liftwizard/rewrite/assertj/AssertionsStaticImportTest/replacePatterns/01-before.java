import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;

class Test
{
	void test()
	{
		List<String> list = new ArrayList<>();
		Assertions.assertThat(list).isEmpty();
		Assertions.assertThat(list).isNotEmpty();
		Assertions.assertThat(list).hasSize(0);
		Assertions.assertThat("text").isEqualTo("text");
		Assertions.assertThat(42).isGreaterThan(0);
		Assertions.assertThat(true).isTrue();

		Assertions.assertThatThrownBy(() ->
		{
			throw new IllegalArgumentException("error");
		}).isInstanceOf(IllegalArgumentException.class);

		Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
		{
			throw new IllegalArgumentException("error");
		});

		Map<String, String> map = new HashMap<>();
		Assertions.assertThat(map).containsKey("key");

		Assertions.fail("Should not reach here");

		Assertions.useDefaultDateFormatsOnly();
	}
}
