import org.eclipse.collections.api.list.MutableList;

class Test {
	String testOtherOptionalCalls(MutableList<String> list) {
		return list.detectOptional(s -> s.length() > 5).orElse("default");
	}
}
