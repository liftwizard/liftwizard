import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.collections.api.block.function.Function;
import org.eclipse.collections.api.block.function.Function0;
import org.eclipse.collections.api.block.function.Function2;
import org.eclipse.collections.api.block.procedure.Procedure;
import org.eclipse.collections.api.block.procedure.Procedure2;
import org.eclipse.collections.impl.block.factory.Functions;
import org.eclipse.collections.impl.block.function.AddFunction;
import org.eclipse.collections.impl.block.function.MultiplyFunction;
import org.eclipse.collections.impl.block.function.PassThruFunction0;
import org.eclipse.collections.impl.block.function.SubtractFunction;
import org.eclipse.collections.impl.block.procedure.CollectionAddProcedure;
import org.eclipse.collections.impl.block.procedure.CollectionRemoveProcedure;
import org.eclipse.collections.impl.block.procedure.MapPutProcedure;

class Test
{
	void test()
	{
		Function2<Integer, Integer, Integer> addInt = AddFunction.INTEGER;
		Function2<Long, Long, Long> addLong = AddFunction.LONG;
		Function2<Double, Double, Double> addDouble = AddFunction.DOUBLE;
		Function2<Float, Float, Float> addFloat = AddFunction.FLOAT;

		Function2<Integer, Integer, Integer> mulInt = MultiplyFunction.INTEGER;
		Function2<Long, Long, Long> mulLong = MultiplyFunction.LONG;
		Function2<Double, Double, Double> mulDouble = MultiplyFunction.DOUBLE;

		Function2<Integer, Integer, Integer> subInt = SubtractFunction.INTEGER;
		Function2<Long, Long, Long> subLong = SubtractFunction.LONG;
		Function2<Double, Double, Double> subDouble = SubtractFunction.DOUBLE;

		Function<String, Integer> stringToInteger = Functions.getStringToInteger();
		Function<String, String> stringTrim = Functions.getStringTrim();
		Function<Object, Class<?>> toClass = Functions.getToClass();
		Function<Object, String> toString = Functions.getToString();

		Map<String, Integer> map = new HashMap<>();
		Procedure2<String, Integer> mapPut = new MapPutProcedure<>(map);

		Function0<String> literalSupplier = new PassThruFunction0<>("hello");
		String value = "world";
		Function0<String> variableSupplier = new PassThruFunction0<>(value);
		Function0<Integer> intSupplier = new PassThruFunction0<>(42);

		List<String> addList = new ArrayList<>();
		addList.forEach(CollectionAddProcedure.on(addList));

		List<String> addCtor1 = new ArrayList<>();
		Procedure<String> addProcedure1 = new CollectionAddProcedure<String>(addCtor1);

		List<String> addCtor2 = new ArrayList<>();
		Procedure<String> addProcedure2 = new CollectionAddProcedure<>(addCtor2);

		List<String> addCtor3 = new ArrayList<>();
		addCtor3.forEach(new CollectionAddProcedure<>(addCtor3));

		List<String> removeList = new ArrayList<>();
		Procedure<String> removeProcedure1 = CollectionRemoveProcedure.on(removeList);

		List<String> removeCtor1 = new ArrayList<>();
		Procedure<String> removeProcedure2 = new CollectionRemoveProcedure<String>(removeCtor1);

		List<String> removeCtor2 = new ArrayList<>();
		Procedure<String> removeProcedure3 = new CollectionRemoveProcedure<>(removeCtor2);
	}
}
