import java.util.function.Consumer;
import org.eclipse.collections.api.block.function.Function;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.block.procedure.Procedure;
import org.eclipse.collections.api.list.MutableList;

class Test {
	void test(
			MutableList<String> list,
			Predicate<String> predicate,
			Function<String, Integer> function,
			Procedure<String> procedure,
			Consumer<String> consumer) {
		// Already simplified - should not change
		MutableList<String> alreadySimple = list.select(predicate);
		MutableList<Integer> alreadySimpleCollect = list.collect(function);
		list.forEach(procedure);

		// Lambda expressions - should not change
		MutableList<String> lambda = list.select(s -> s.isEmpty());
		MutableList<Integer> lambdaCollect = list.collect(s -> s.length());
		list.forEach(System.out::println);

		// Method references to other methods - should not change
		MutableList<String> methodRef = list.select(String::isEmpty);
		MutableList<Integer> methodRefCollect = list.collect(String::length);
		list.forEach(System.out::println);

		// Method references to non-functional-interface methods - should not change
		MutableList<String> toString = list.collect(Object::toString);
		MutableList<Integer> hashCode = list.collect(Object::hashCode);
	}

	void consumerToProcedureVariable(Consumer<String> consumer) {
		// Consumer is NOT assignable to Procedure - should NOT simplify
		Procedure<String> procedure = consumer::accept;
		procedure.value("test");
	}

	// Methods with single functional parameter (no overloads)
	void acceptProcedure(Procedure<String> procedure) {}
	void acceptPredicate(Predicate<String> predicate) {}
	void acceptFunction(Function<String, Integer> function) {}

	void consumerToProcedureMethodArg(Consumer<String> consumer) {
		// Consumer is NOT assignable to Procedure - should NOT simplify
		acceptProcedure(consumer::accept);
	}

	void jdkPredicateToEcPredicateMethodArg(java.util.function.Predicate<String> jdkPredicate) {
		// JDK Predicate is NOT assignable to EC Predicate - should NOT simplify
		acceptPredicate(jdkPredicate::test);
	}

	void jdkFunctionToEcFunctionMethodArg(java.util.function.Function<String, Integer> jdkFunction) {
		// JDK Function is NOT assignable to EC Function - should NOT simplify
		acceptFunction(jdkFunction::apply);
	}

}
