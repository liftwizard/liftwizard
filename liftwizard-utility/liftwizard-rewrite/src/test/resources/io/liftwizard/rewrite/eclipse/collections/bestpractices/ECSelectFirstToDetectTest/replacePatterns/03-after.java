import java.util.Optional;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.list.ImmutableList;

class TestImmutable
{
	Optional<String> test(ImmutableList<String> list, Predicate<String> predicate)
	{
		return list.detectOptional(predicate);
	}
}
