import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.impl.utility.ArrayIterate;

class TestArrayIterate
{
	boolean test(String[] array, Predicate<String> predicate)
	{
		return ArrayIterate.anySatisfy(array, predicate);
	}
}
