import org.eclipse.collections.api.list.MutableList;

class Test {
	void test(MutableList<String> list) {
		boolean otherComparison = list.count(s -> s.length() > 5) >= 2;
		int countResult = list.count(s -> s.length() > 5);
	}
}
