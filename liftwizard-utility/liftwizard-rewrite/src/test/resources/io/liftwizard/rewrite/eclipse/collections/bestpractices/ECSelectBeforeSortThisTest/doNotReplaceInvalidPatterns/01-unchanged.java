import org.eclipse.collections.api.list.MutableList;

class Test {
	MutableList<String> selectBeforeSortThis(MutableList<String> list) {
		return list.select(s -> s.length() > 3).sortThis();
	}

	MutableList<String> sortThisAlone(MutableList<String> list) {
		return list.sortThis();
	}

	MutableList<Integer> sortThisFollowedByCollect(MutableList<String> list) {
		return list.sortThis().collect(String::length);
	}
}
