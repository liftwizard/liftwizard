import java.util.ArrayList;
import java.util.List;

class TestJavaList {
	int test() {
		List<String> list = new ArrayList<>();
		return (int) list.stream().filter(s -> s.length() > 5).count();
	}
}
