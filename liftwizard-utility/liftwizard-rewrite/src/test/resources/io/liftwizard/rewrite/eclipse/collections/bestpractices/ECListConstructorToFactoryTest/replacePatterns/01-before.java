import java.util.Map;
import org.eclipse.collections.api.factory.set.ImmutableSetFactory;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.set.immutable.ImmutableSetFactoryImpl;

class Test<T>
{
	// Field declarations - interface type with various constructors
	private final MutableList<String> fieldInterfaceEmpty = new FastList<>();
	private final MutableList<Integer> fieldInterfaceCapacity = new FastList<>(10);
	private final MutableList<String> fieldInterfaceCollection = new FastList<>(fieldInterfaceEmpty);

	// FieldAccess expression - should be ignored without crashing
	public static final ImmutableSetFactory immutable = ImmutableSetFactoryImpl.INSTANCE;
	public static final Object INSTANCE = java.util.Collections.EMPTY_SET;
	public static final java.util.List<?> EMPTY_LIST = java.util.Collections.EMPTY_LIST;

	void test()
	{
		// Local variables - various generic forms
		MutableList<String> diamondList = new FastList<>();
		MutableList rawList = new FastList();
		MutableList<Map<String, Integer>> nestedGenerics = new FastList<>();
		MutableList<? extends Number> wildcardGenerics = new FastList<>();

		// Explicit type parameters
		MutableList<String> explicitSimple = new FastList<String>();
		MutableList<Map<String, Integer>> explicitNested = new FastList<Map<String, Integer>>();
		MutableList<MutableList<T>> nestedTypeParam = new FastList<MutableList<T>>();

		// Fully qualified types
		org.eclipse.collections.api.list.MutableList<String> fullyQualified =
			new org.eclipse.collections.impl.list.mutable.FastList<>();

		// Initial capacity constructor
		MutableList<String> withCapacity = new FastList<>(16);
		MutableList<Integer> withCapacity32 = new FastList<>(32);

		// Collection constructor
		MutableList<String> listFromOther = new FastList<>(diamondList);
	}

	// Return statement context
	MutableList<T> newEmpty()
	{
		return new FastList<>();
	}
}
