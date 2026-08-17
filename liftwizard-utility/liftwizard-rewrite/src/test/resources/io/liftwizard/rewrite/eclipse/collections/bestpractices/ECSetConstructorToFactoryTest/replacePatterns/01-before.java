import java.util.List;
import org.eclipse.collections.api.factory.set.ImmutableSetFactory;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.set.immutable.ImmutableSetFactoryImpl;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

class Test<T>
{
	// Field declarations - interface type with various constructors
	private final MutableSet<String> fieldInterfaceEmpty = new UnifiedSet<>();
	private final MutableSet<Integer> fieldInterfaceCapacity = new UnifiedSet<>(16);
	private final MutableSet<String> fieldInterfaceCollection = new UnifiedSet<>(fieldInterfaceEmpty);

	// FieldAccess expression - should be ignored without crashing
	public static final ImmutableSetFactory immutable = ImmutableSetFactoryImpl.INSTANCE;
	public static final Object INSTANCE = java.util.Collections.EMPTY_SET;
	public static final java.util.List<?> EMPTY_LIST = java.util.Collections.EMPTY_LIST;

	void test()
	{
		// Local variables - various generic forms
		MutableSet<String> diamondSet = new UnifiedSet<>();
		MutableSet rawSet = new UnifiedSet();
		MutableSet<List<Integer>> nestedGenerics = new UnifiedSet<>();
		MutableSet<? extends Number> wildcardGenerics = new UnifiedSet<>();

		// Explicit type parameters
		MutableSet<String> explicitSimple = new UnifiedSet<String>();
		MutableSet<List<String>> explicitNested = new UnifiedSet<List<String>>();
		MutableSet<MutableSet<T>> nestedTypeParam = new UnifiedSet<MutableSet<T>>();

		// Fully qualified types
		org.eclipse.collections.api.set.MutableSet<String> fullyQualified =
			new org.eclipse.collections.impl.set.mutable.UnifiedSet<>();

		// Initial capacity constructor
		MutableSet<String> withCapacity = new UnifiedSet<>(16);
		MutableSet<Integer> withCapacity32 = new UnifiedSet<>(32);

		// Collection constructor
		MutableSet<String> setFromOther = new UnifiedSet<>(diamondSet);
	}
}

class A<T>
{
	@Override
	public MutableSet<T> newEmpty()
	{
		return new UnifiedSet<>();
	}
}
