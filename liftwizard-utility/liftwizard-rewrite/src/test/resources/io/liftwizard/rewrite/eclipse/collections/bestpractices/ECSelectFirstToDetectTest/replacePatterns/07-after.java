import java.util.List;
import java.util.Optional;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.impl.utility.ListIterate;

class TestListIterate
{
	Optional<String> test(List<String> list, Predicate<String> predicate)
	{
		return ListIterate.detectOptional(list, predicate);
	}
}
