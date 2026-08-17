import java.util.Collections;
import java.util.Set;
import org.eclipse.collections.api.factory.set.FixedSizeSetFactory;
import org.eclipse.collections.api.factory.set.ImmutableSetFactory;
import org.eclipse.collections.api.factory.set.MultiReaderSetFactory;
import org.eclipse.collections.api.factory.set.MutableSetFactory;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.set.fixed.FixedSizeSetFactoryImpl;
import org.eclipse.collections.impl.set.immutable.ImmutableSetFactoryImpl;
import org.eclipse.collections.impl.set.mutable.MultiReaderMutableSetFactory;
import org.eclipse.collections.impl.set.mutable.MutableSetFactoryImpl;

class Test
{
	// Concrete type declarations - should NOT be replaced
	private final FastList<String> fieldConcreteType = new FastList<>();

	void test()
	{
		FastList<String> concreteTypeEmpty = new FastList<>();
		FastList<String> concreteTypeCapacity = new FastList<>(10);
		FastList<String> concreteTypeCollection = new FastList<>(concreteTypeEmpty);
	}

	// FieldAccess expressions - should not crash
	private static final Set<?> EMPTY = Collections.EMPTY_SET;
}

// Multiple FieldAccess factory patterns - should not crash
final class Sets
{
	public static final ImmutableSetFactory immutable = ImmutableSetFactoryImpl.INSTANCE;
	public static final FixedSizeSetFactory fixedSize = FixedSizeSetFactoryImpl.INSTANCE;
	public static final MutableSetFactory mutable = MutableSetFactoryImpl.INSTANCE;
	public static final MultiReaderSetFactory multiReader = MultiReaderMutableSetFactory.INSTANCE;
}
