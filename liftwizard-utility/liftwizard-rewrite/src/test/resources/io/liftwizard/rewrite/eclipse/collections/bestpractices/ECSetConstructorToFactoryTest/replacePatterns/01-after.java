import java.util.List;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.factory.set.ImmutableSetFactory;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.set.immutable.ImmutableSetFactoryImpl;

class Test<T>
{
	// Field declarations - interface type with various constructors
	private final MutableSet<String> fieldInterfaceEmpty = Sets.mutable.empty();
	private final MutableSet<Integer> fieldInterfaceCapacity = Sets.mutable.withInitialCapacity(16);
	private final MutableSet<String> fieldInterfaceCollection = Sets.mutable.withAll(fieldInterfaceEmpty);

	// FieldAccess expression - should be ignored without crashing
	public static final ImmutableSetFactory immutable = ImmutableSetFactoryImpl.INSTANCE;
	public static final Object INSTANCE = java.util.Collections.EMPTY_SET;
	public static final java.util.List<?> EMPTY_LIST = java.util.Collections.EMPTY_LIST;

	void test()
	{
		// Local variables - various generic forms
		MutableSet<String> diamondSet = Sets.mutable.empty();
		MutableSet rawSet = Sets.mutable.empty();
		MutableSet<List<Integer>> nestedGenerics = Sets.mutable.empty();
		MutableSet<? extends Number> wildcardGenerics = Sets.mutable.empty();

		// Explicit type parameters
		MutableSet<String> explicitSimple = Sets.mutable.<String>empty();
		MutableSet<List<String>> explicitNested = Sets.mutable.<List<String>>empty();
		MutableSet<MutableSet<T>> nestedTypeParam = Sets.mutable.<MutableSet<T>>empty();

		// Fully qualified types
		org.eclipse.collections.api.set.MutableSet<String> fullyQualified =
				Sets.mutable.empty();

		// Initial capacity constructor
		MutableSet<String> withCapacity = Sets.mutable.withInitialCapacity(16);
		MutableSet<Integer> withCapacity32 = Sets.mutable.withInitialCapacity(32);

		// Collection constructor
		MutableSet<String> setFromOther = Sets.mutable.withAll(diamondSet);
	}
}

class A<T>
{
	@Override
	public MutableSet<T> newEmpty()
	{
		return Sets.mutable.empty();
	}
}
