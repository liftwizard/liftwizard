import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.impl.utility.ArrayIterate;

class TestArrayIterate
{
	int test(String[] array, Predicate<String> predicate)
	{
		return ArrayIterate.count(array, predicate);
	}
}
