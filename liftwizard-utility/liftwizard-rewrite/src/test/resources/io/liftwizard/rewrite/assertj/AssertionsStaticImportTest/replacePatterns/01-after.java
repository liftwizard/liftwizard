import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.useDefaultDateFormatsOnly;

class Test
{
	void test()
	{
		List<String> list = new ArrayList<>();
		assertThat(list).isEmpty();
		assertThat(list).isNotEmpty();
		assertThat(list).hasSize(0);
		assertThat("text").isEqualTo("text");
		assertThat(42).isGreaterThan(0);
		assertThat(true).isTrue();

		assertThatThrownBy(() ->
		{
			throw new IllegalArgumentException("error");
		}).isInstanceOf(IllegalArgumentException.class);

		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
		{
			throw new IllegalArgumentException("error");
		});

		Map<String, String> map = new HashMap<>();
		assertThat(map).containsKey("key");

		fail("Should not reach here");

		useDefaultDateFormatsOnly();
	}
}
