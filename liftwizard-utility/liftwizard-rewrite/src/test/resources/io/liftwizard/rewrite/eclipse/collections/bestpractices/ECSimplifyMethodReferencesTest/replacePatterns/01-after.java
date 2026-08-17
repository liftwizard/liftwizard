import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.eclipse.collections.api.block.function.Function;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.block.procedure.Procedure;
import org.eclipse.collections.api.list.MutableList;

class Test
{
	void ecCollectionMethods(
		MutableList<String> list,
		Predicate<String> predicate,
		Function<String, Integer> function,
		Procedure<String> procedure,
		java.util.function.Predicate<String> jdkPredicate,
		java.util.function.Function<String, Integer> jdkFunction,
		Consumer<String> consumer
	)
	{
		// Eclipse Collections Predicate - accept method
		MutableList<String> selectAccept = list.select(predicate);
		MutableList<String> rejectAccept = list.reject(predicate);

		// Eclipse Collections Predicate - test method (JDK compatible)
		MutableList<String> selectTest = list.select(predicate);
		MutableList<String> rejectTest = list.reject(predicate);

		// Eclipse Collections Function - valueOf method
		MutableList<Integer> collectValueOf = list.collect(function);

		// Eclipse Collections Function - apply method (JDK compatible)
		MutableList<Integer> collectApply = list.collect(function);

		// Eclipse Collections Procedure - value method
		list.forEach(procedure);

		// Eclipse Collections Procedure - accept method (JDK compatible)
		list.forEach(procedure);

		// JDK Predicate - test method
		MutableList<String> jdkSelectTest = list.select(jdkPredicate::test);

		// JDK Function - apply method
		MutableList<Integer> jdkCollectApply = list.collect(jdkFunction::apply);

		// JDK Consumer - accept method
		list.forEach(consumer::accept);

		// Works in chained calls
		MutableList<Integer> chained = list.select(predicate).collect(function);
	}

	void jdkOnlyMethodsWithJdkTypes(
		List<String> list,
		java.util.function.Predicate<String> predicate,
		java.util.function.Function<String, Integer> function,
		Consumer<String> consumer
	)
	{
		// JDK Predicate with Collection.removeIf
		list.removeIf(predicate);

		// JDK Consumer with Iterable.forEach
		list.forEach(consumer);

		// JDK Function with Stream.map
		list.stream().map(function).collect(Collectors.toList());
	}

	void jdkOnlyMethodsWithEcTypes(
		List<String> list,
		Predicate<String> predicate,
		Function<String, Integer> function,
		Procedure<String> procedure
	)
	{
		// EC Predicate with Collection.removeIf
		// (EC Predicate extends JDK Predicate, so this is safe)
		list.removeIf(predicate);
		list.removeIf(predicate);

		// EC Procedure with Iterable.forEach
		// (EC Procedure extends JDK Consumer, so this is safe)
		list.forEach(procedure);
		list.forEach(procedure);

		// EC Function with Stream.map
		// (EC Function extends JDK Function, so this is safe)
		list.stream().map(function).collect(Collectors.toList());
		list.stream().map(function).collect(Collectors.toList());
	}
}
