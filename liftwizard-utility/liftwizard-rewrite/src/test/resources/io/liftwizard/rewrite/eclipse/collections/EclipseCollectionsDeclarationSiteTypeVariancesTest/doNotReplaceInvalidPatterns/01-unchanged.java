import org.eclipse.collections.api.block.function.Function;
import org.eclipse.collections.api.block.function.Function0;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.block.procedure.Procedure;
import org.eclipse.collections.api.list.MutableList;

class Test {
	// Already has correct variance - should not change
	<T> MutableList<T> filter(MutableList<T> list, Predicate<? super T> predicate) {
		return list.select(predicate);
	}

	<T> void process(MutableList<T> list, Procedure<? super T> procedure) {
		list.forEach(procedure);
	}

	<T, R> MutableList<R> transform(MutableList<T> list, Function<? super T, ? extends R> function) {
		return list.collect(function);
	}

	<T> T getOrDefault(T value, Function0<? extends T> factory) {
		return value != null ? value : factory.value();
	}
}
