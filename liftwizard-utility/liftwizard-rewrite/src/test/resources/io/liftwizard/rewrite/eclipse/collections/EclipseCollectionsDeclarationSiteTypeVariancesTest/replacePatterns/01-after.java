import org.eclipse.collections.api.block.function.Function;
import org.eclipse.collections.api.block.function.Function0;
import org.eclipse.collections.api.block.function.Function2;
import org.eclipse.collections.api.block.function.primitive.IntFunction;
import org.eclipse.collections.api.block.function.primitive.IntToObjectFunction;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.block.predicate.Predicate2;
import org.eclipse.collections.api.block.procedure.Procedure;
import org.eclipse.collections.api.block.procedure.Procedure2;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	// Predicate<IN> -> Predicate<? super IN>
	<T> MutableList<T> filter(MutableList<T> list, Predicate<? super T> predicate)
	{
		return list.select(predicate);
	}

	// Predicate2<IN, IN> -> Predicate2<? super IN, ? super IN>
	<T, P> MutableList<T> filterWith(MutableList<T> list, Predicate2<? super T, ? super P> predicate, P param)
	{
		return list.selectWith(predicate, param);
	}

	// Procedure<IN> -> Procedure<? super IN>
	<T> void process(MutableList<T> list, Procedure<? super T> procedure)
	{
		list.forEach(procedure);
	}

	// Procedure2<IN, IN> -> Procedure2<? super IN, ? super IN>
	<T, P> void processWith(MutableList<T> list, Procedure2<? super T, ? super P> procedure, P param)
	{
		list.forEachWith(procedure, param);
	}

	// Function<IN, OUT> -> Function<? super IN, ? extends OUT>
	<T, R> MutableList<R> transform(MutableList<T> list, Function<? super T, ? extends R> function)
	{
		return list.collect(function);
	}

	// Function0<OUT> -> Function0<? extends OUT>
	<T> T getOrDefault(T value, Function0<? extends T> factory)
	{
		return value != null ? value : factory.value();
	}

	// Function2<IN, IN, OUT> -> Function2<? super IN, ? super IN, ? extends OUT>
	<T, R> R reduce(MutableList<T> list, R initial, Function2<? super R, ? super T, ? extends R> function)
	{
		return list.injectInto(initial, function);
	}

	// IntFunction<IN> -> IntFunction<? super IN>
	<T> long sum(MutableList<T> list, IntFunction<? super T> function)
	{
		return list.sumOfInt(function);
	}

	// IntToObjectFunction<OUT> -> IntToObjectFunction<? extends OUT>
	<T> MutableList<T> collect(int[] array, IntToObjectFunction<? extends T> function)
	{
		return null;
	}
}
