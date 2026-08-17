import java.util.Optional;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.impl.utility.ArrayIterate;

class TestArrayIterate
{
	Optional<String> test(String[] array, Predicate<String> predicate)
	{
		return ArrayIterate.detectOptional(array, predicate);
	}
}
