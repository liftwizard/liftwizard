import org.eclipse.collections.api.factory.set.FixedSizeSetFactory;
import org.eclipse.collections.api.factory.set.ImmutableSetFactory;
import org.eclipse.collections.api.factory.set.MutableSetFactory;
import org.eclipse.collections.impl.set.fixed.FixedSizeSetFactoryImpl;
import org.eclipse.collections.impl.set.immutable.ImmutableSetFactoryImpl;
import org.eclipse.collections.impl.set.mutable.MutableSetFactoryImpl;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.Collections;
import java.util.Set;

class Test {
	// Concrete type declarations - should NOT be replaced
	private final UnifiedSet<String> fieldConcreteType = new UnifiedSet<>();

	void test() {
		UnifiedSet<String> concreteTypeEmpty = new UnifiedSet<>();
		UnifiedSet<String> concreteTypeCapacity = new UnifiedSet<>(10);
		UnifiedSet<String> concreteTypeCollection = new UnifiedSet<>(concreteTypeEmpty);
	}

	// FieldAccess expressions - should not crash
	private static final Set<?> EMPTY = Collections.EMPTY_SET;
}

// Multiple FieldAccess factory patterns - should not crash
final class Sets {
	public static final ImmutableSetFactory immutable = ImmutableSetFactoryImpl.INSTANCE;
	public static final FixedSizeSetFactory fixedSize = FixedSizeSetFactoryImpl.INSTANCE;
	public static final MutableSetFactory mutable = MutableSetFactoryImpl.INSTANCE;
}
