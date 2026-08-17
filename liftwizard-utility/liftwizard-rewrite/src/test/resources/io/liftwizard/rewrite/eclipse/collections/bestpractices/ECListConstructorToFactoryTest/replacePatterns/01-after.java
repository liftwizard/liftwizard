import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.set.ImmutableSetFactory;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.set.immutable.ImmutableSetFactoryImpl;

class Test<T>
{
	// Field declarations - interface type with various constructors
	private final MutableList<String> fieldInterfaceEmpty = Lists.mutable.empty();
	private final MutableList<Integer> fieldInterfaceCapacity = Lists.mutable.withInitialCapacity(10);
	private final MutableList<String> fieldInterfaceCollection = Lists.mutable.withAll(fieldInterfaceEmpty);

	// FieldAccess expression - should be ignored without crashing
	public static final ImmutableSetFactory immutable = ImmutableSetFactoryImpl.INSTANCE;
	public static final Object INSTANCE = java.util.Collections.EMPTY_SET;
	public static final java.util.List<?> EMPTY_LIST = java.util.Collections.EMPTY_LIST;

	void test()
	{
		// Local variables - various generic forms
		MutableList<String> diamondList = Lists.mutable.empty();
		MutableList rawList = Lists.mutable.empty();
		MutableList<Map<String, Integer>> nestedGenerics = Lists.mutable.empty();
		MutableList<? extends Number> wildcardGenerics = Lists.mutable.empty();

		// Explicit type parameters
		MutableList<String> explicitSimple = Lists.mutable.<String>empty();
		MutableList<Map<String, Integer>> explicitNested = Lists.mutable.<Map<String, Integer>>empty();
		MutableList<MutableList<T>> nestedTypeParam = Lists.mutable.<MutableList<T>>empty();

		// Fully qualified types
		org.eclipse.collections.api.list.MutableList<String> fullyQualified =
				Lists.mutable.empty();

		// Initial capacity constructor
		MutableList<String> withCapacity = Lists.mutable.withInitialCapacity(16);
		MutableList<Integer> withCapacity32 = Lists.mutable.withInitialCapacity(32);

		// Collection constructor
		MutableList<String> listFromOther = Lists.mutable.withAll(diamondList);
	}

	// Return statement context
	MutableList<T> newEmpty()
	{
		return Lists.mutable.empty();
	}
}
