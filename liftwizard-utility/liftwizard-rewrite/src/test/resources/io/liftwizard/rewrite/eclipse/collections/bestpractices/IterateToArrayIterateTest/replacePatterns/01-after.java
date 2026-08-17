import org.eclipse.collections.api.block.function.Function;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.impl.utility.ArrayIterate;

class Test
{
	void testWithPredicate(String[] array)
	{
		Predicate<String> predicate = (s) -> s.length() > 5;
		boolean result = ArrayIterate.anySatisfy(array, predicate);
	}

	void testMultipleMethods(String[] names, Integer[] numbers)
	{
		Predicate<String> predicate = (s) -> s.length() > 5;
		Function<Integer, String> function = Object::toString;
		boolean any = ArrayIterate.anySatisfy(names, predicate);
		boolean all = ArrayIterate.allSatisfy(names, predicate);
		boolean none = ArrayIterate.noneSatisfy(names, predicate);
		String detected = ArrayIterate.detect(names, predicate);
		int count = ArrayIterate.count(numbers, (n) -> n > 0);
		java.util.Collection<String> collected = ArrayIterate.collect(numbers, function);
	}

	void testWithLambda(String[] array)
	{
		boolean result = ArrayIterate.anySatisfy(array, (s) -> s.startsWith("test"));
	}

	void testWithMethodReference(String[] array)
	{
		boolean result = ArrayIterate.anySatisfy(array, String::isEmpty);
	}
}
