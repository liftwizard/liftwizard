import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.collections.api.block.function.Function;
import org.eclipse.collections.api.block.function.Function0;
import org.eclipse.collections.api.block.function.Function2;
import org.eclipse.collections.api.block.procedure.Procedure;
import org.eclipse.collections.api.block.procedure.Procedure2;

class Test
{
	void test()
	{
		Function2<Integer, Integer, Integer> addInt = Integer::sum;
		Function2<Long, Long, Long> addLong = Long::sum;
		Function2<Double, Double, Double> addDouble = Double::sum;
		Function2<Float, Float, Float> addFloat = Float::sum;

		Function2<Integer, Integer, Integer> mulInt = (Integer a, Integer b) -> a * b;
		Function2<Long, Long, Long> mulLong = (Long a, Long b) -> a * b;
		Function2<Double, Double, Double> mulDouble = (Double a, Double b) -> a * b;

		Function2<Integer, Integer, Integer> subInt = (Integer a, Integer b) -> a - b;
		Function2<Long, Long, Long> subLong = (Long a, Long b) -> a - b;
		Function2<Double, Double, Double> subDouble = (Double a, Double b) -> a - b;

		Function<String, Integer> stringToInteger = Integer::valueOf;
		Function<String, String> stringTrim = String::trim;
		Function<Object, Class<?>> toClass = Object::getClass;
		Function<Object, String> toString = Object::toString;

		Map<String, Integer> map = new HashMap<>();
		Procedure2<String, Integer> mapPut = map::put;

		Function0<String> literalSupplier = () -> "hello";
		String value = "world";
		Function0<String> variableSupplier = () -> value;
		Function0<Integer> intSupplier = () -> 42;

		List<String> addList = new ArrayList<>();
		addList.forEach(addList::add);

		List<String> addCtor1 = new ArrayList<>();
		Procedure<String> addProcedure1 = addCtor1::add;

		List<String> addCtor2 = new ArrayList<>();
		Procedure<String> addProcedure2 = addCtor2::add;

		List<String> addCtor3 = new ArrayList<>();
		addCtor3.forEach(addCtor3::add);

		List<String> removeList = new ArrayList<>();
		Procedure<String> removeProcedure1 = removeList::remove;

		List<String> removeCtor1 = new ArrayList<>();
		Procedure<String> removeProcedure2 = removeCtor1::remove;

		List<String> removeCtor2 = new ArrayList<>();
		Procedure<String> removeProcedure3 = removeCtor2::remove;
	}
}
