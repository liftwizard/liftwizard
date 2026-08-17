import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	boolean nonNegatedCalls(MutableList<String> list)
	{
		return list.isEmpty() || list.notEmpty();
	}

	boolean jdkCollectionShouldNotTransform(java.util.List<String> list)
	{
		return !list.isEmpty();
	}
}

interface IsEmptyImpl<T>
	extends RichIterable<T>
{
	default boolean isEmpty()
	{
		return !this.notEmpty();
	}
}

interface NotEmptyImpl<T>
	extends RichIterable<T>
{
	default boolean notEmpty()
	{
		return !this.isEmpty();
	}
}
