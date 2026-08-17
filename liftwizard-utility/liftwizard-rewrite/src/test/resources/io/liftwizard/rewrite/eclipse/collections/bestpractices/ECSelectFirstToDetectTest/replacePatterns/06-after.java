import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.impl.utility.ArrayIterate;

class TestArrayIterateGetFirst
{
	String test(String[] array, Predicate<String> predicate)
	{
		return ArrayIterate.detect(array, predicate);
	}
}
