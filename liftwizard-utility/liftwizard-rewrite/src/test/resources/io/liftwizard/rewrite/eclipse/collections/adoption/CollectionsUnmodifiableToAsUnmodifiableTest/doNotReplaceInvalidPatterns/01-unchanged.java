import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Test {
	void test() {
		List<String> list = new ArrayList<>();
		List<String> unmodifiableList = Collections.unmodifiableList(list);
	}
}
