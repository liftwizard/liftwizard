import java.util.Arrays;
import java.util.List;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.impl.utility.Iterate;

class Test {
	boolean testNonArraysAsListCalls(List<String> list) {
		return Iterate.anySatisfy(list, s -> s.length() > 5);
	}

	boolean testWithMultipleArraysAsListArguments(String[] array) {
		Predicate<String[]> predicate = arr -> arr.length > 0;
		return Iterate.anySatisfy(Arrays.asList(array, new String[]{"extra"}), predicate);
	}
}
