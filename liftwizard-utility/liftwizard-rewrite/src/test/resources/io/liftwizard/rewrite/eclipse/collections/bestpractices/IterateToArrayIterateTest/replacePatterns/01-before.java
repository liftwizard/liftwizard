import java.util.Arrays;
import org.eclipse.collections.api.block.function.Function;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.impl.utility.Iterate;

class Test
{
	void testWithPredicate(String[] array)
	{
		Predicate<String> predicate = (s) -> s.length() > 5;
		boolean result = Iterate.anySatisfy(Arrays.asList(array), predicate);
	}

	void testMultipleMethods(String[] names, Integer[] numbers)
	{
		Predicate<String> predicate = (s) -> s.length() > 5;
		Function<Integer, String> function = Object::toString;
		boolean any = Iterate.anySatisfy(Arrays.asList(names), predicate);
		boolean all = Iterate.allSatisfy(Arrays.asList(names), predicate);
		boolean none = Iterate.noneSatisfy(Arrays.asList(names), predicate);
		String detected = Iterate.detect(Arrays.asList(names), predicate);
		int count = Iterate.count(Arrays.asList(numbers), (n) -> n > 0);
		java.util.Collection<String> collected = Iterate.collect(Arrays.asList(numbers), function);
	}

	void testWithLambda(String[] array)
	{
		boolean result = Iterate.anySatisfy(Arrays.asList(array), (s) -> s.startsWith("test"));
	}

	void testWithMethodReference(String[] array)
	{
		boolean result = Iterate.anySatisfy(Arrays.asList(array), String::isEmpty);
	}
}
