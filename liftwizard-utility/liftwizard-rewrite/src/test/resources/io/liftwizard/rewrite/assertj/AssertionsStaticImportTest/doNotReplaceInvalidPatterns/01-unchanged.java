import java.util.List;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class Test {
	void test() {
		List<String> list = new ArrayList<>();
		assertThat(list).isEmpty();
	}
}
