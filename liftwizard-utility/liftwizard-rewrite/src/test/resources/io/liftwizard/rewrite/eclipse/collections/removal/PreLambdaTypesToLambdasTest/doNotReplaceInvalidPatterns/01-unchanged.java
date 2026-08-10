import org.eclipse.collections.api.block.function.Function2;
import org.eclipse.collections.api.block.function.primitive.IntObjectToIntFunction;
import org.eclipse.collections.api.block.procedure.Procedure;
import org.eclipse.collections.api.block.procedure.primitive.IntProcedure;
import org.eclipse.collections.impl.block.function.AddFunction;
import org.eclipse.collections.impl.block.procedure.CollectionAddProcedure;
import org.eclipse.collections.impl.block.procedure.CollectionRemoveProcedure;
import java.util.ArrayList;
import java.util.List;

class Test {
	static void accept(Object value) {}
	static void forEach(Procedure<? super Integer> procedure) {}
	static void forEach(IntProcedure procedure) {}

	static <T> Integer injectInto(int identity, Iterable<T> iterable, IntObjectToIntFunction<? super T> fn) { return 0; }
	static <T> Integer injectInto(int identity, Iterable<T> iterable, Function2<? super Integer, ? super T, ? extends Integer> fn) { return 0; }

	void test() {
		List<String> concreteAdd = new ArrayList<>();
		CollectionAddProcedure<String> addConcrete = new CollectionAddProcedure<>(concreteAdd);

		List<String> concreteRemove = new ArrayList<>();
		CollectionRemoveProcedure<String> removeConcrete = new CollectionRemoveProcedure<>(concreteRemove);

		List<String> objectArg = new ArrayList<>();
		accept(CollectionAddProcedure.on(objectArg));
		accept(new CollectionAddProcedure<>(objectArg));
		accept(CollectionRemoveProcedure.on(objectArg));
		accept(new CollectionRemoveProcedure<>(objectArg));
		accept(1L, "double", AddFunction.DOUBLE);

		Procedure<String> addNullReceiver = CollectionAddProcedure.on(null);
		Procedure<String> removeNullReceiver = CollectionRemoveProcedure.on(null);

		List<Integer> ambiguous = new ArrayList<>();
		forEach(CollectionAddProcedure.on(ambiguous));

		Iterable<Integer> iterable = List.of(1, 2, 3);
		Integer result = injectInto(1, iterable, AddFunction.INTEGER);
	}

	static void accept(long a, String name, Object value) {}
}
