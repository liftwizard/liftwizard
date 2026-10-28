import java.util.List;
import java.util.Map;
import java.util.Optional;

class Test
{
	void noElse(List<String> list, boolean flag, String value)
	{
		if (flag)
		{
			list.add(value);
		}
	}

	void twoDifferences(
		Map<String, String> map,
		boolean flag,
		String firstKey,
		String firstValue,
		String secondKey,
		String secondValue
	)
	{
		if (flag)
		{
			map.put(firstKey, firstValue);
		}
		else
		{
			map.put(secondKey, secondValue);
		}
	}

	void differentMethodName(List<String> list, boolean flag, String value)
	{
		if (flag)
		{
			list.add(value);
		}
		else
		{
			list.remove(value);
		}
	}

	void differentArgumentCount(List<String> list, boolean flag, String value)
	{
		if (flag)
		{
			list.add(value);
		}
		else
		{
			list.add(0, value);
		}
	}

	void multipleStatements(List<String> list, boolean flag, String first, String second)
	{
		if (flag)
		{
			list.add(first);
			list.add(second);
		}
		else
		{
			list.add(second);
		}
	}

	void identicalBranches(List<String> list, boolean flag, String value)
	{
		if (flag)
		{
			list.add(value);
		}
		else
		{
			list.add(value);
		}
	}

	void differentVariables(boolean flag, String value)
	{
		String first;
		String second;
		if (flag)
		{
			first = value;
		}
		else
		{
			second = value;
		}
	}

	void differentStatementKinds(List<String> list, boolean flag, String value)
	{
		String result;
		if (flag)
		{
			list.add(value);
		}
		else
		{
			result = value;
		}
	}

	void assignLambda(boolean flag)
	{
		Runnable runnable;
		if (flag)
		{
			runnable = () -> this.first();
		}
		else
		{
			runnable = () -> this.second();
		}
		runnable.run();
	}

	void lambdaArgument(Optional<String> optional, boolean flag)
	{
		if (flag)
		{
			optional.ifPresent((value) -> this.consume(value));
		}
		else
		{
			optional.ifPresent((value) -> this.consumeOther(value));
		}
	}

	void memberReferenceArgument(Optional<String> optional, boolean flag)
	{
		if (flag)
		{
			optional.ifPresent(this::consume);
		}
		else
		{
			optional.ifPresent(this::consumeOther);
		}
	}

	void first()
	{
	}

	void second()
	{
	}

	void consume(String value)
	{
	}

	void consumeOther(String value)
	{
	}
}
