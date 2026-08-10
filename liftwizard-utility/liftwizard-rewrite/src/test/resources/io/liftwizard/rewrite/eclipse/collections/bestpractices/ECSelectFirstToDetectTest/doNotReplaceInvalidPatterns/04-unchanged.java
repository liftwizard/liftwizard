import java.util.ArrayList;
import java.util.List;

class TestJavaList {
	void test() {
		List<String> list = new ArrayList<>();
		list.stream().filter(s -> s.length() > 5).findFirst();
	}
}
