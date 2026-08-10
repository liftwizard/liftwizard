import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Test {
	void test() {
		assertThatThrownBy(() -> {
			throw new IllegalArgumentException("error");
		}).isInstanceOf(IllegalArgumentException.class);
	}
}
