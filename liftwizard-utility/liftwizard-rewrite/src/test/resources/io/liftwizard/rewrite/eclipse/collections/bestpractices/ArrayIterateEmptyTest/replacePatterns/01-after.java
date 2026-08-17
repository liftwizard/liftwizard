import org.eclipse.collections.impl.utility.ArrayIterate;

class Test
{
	void test(String[] strings, Object[] objects)
	{
		boolean isEmptyNullOrLength = ArrayIterate.isEmpty(strings);
		boolean isEmptyNullOrLengthLessOrEqual = ArrayIterate.isEmpty(strings);
		boolean isEmptyNullOrLengthLessThanOne = ArrayIterate.isEmpty(strings);
		boolean notEmptyNotNullAndLength = ArrayIterate.notEmpty(objects);
		boolean notEmptyNotNullAndLengthNotEqual = ArrayIterate.notEmpty(objects);
		boolean notEmptyNotNullAndLengthGreaterOrEqual = ArrayIterate.notEmpty(objects);

		if (ArrayIterate.isEmpty(strings))
		{
		}

		if (ArrayIterate.isEmpty(strings))
		{
		}

		if (ArrayIterate.isEmpty(strings))
		{
		}

		if (ArrayIterate.notEmpty(objects))
		{
		}

		if (ArrayIterate.notEmpty(objects))
		{
		}

		if (ArrayIterate.notEmpty(objects))
		{
		}
	}

	boolean testNegatedArrayIterateIsEmpty(String[] array)
	{
		return ArrayIterate.notEmpty(array);
	}

	boolean testNegatedArrayIterateNotEmpty(String[] array)
	{
		return ArrayIterate.isEmpty(array);
	}

	void testMultipleNegated(String[] array1, Object[] array2)
	{
		if (ArrayIterate.notEmpty(array1))
		{
		}

		if (ArrayIterate.isEmpty(array2))
		{
		}
	}
}
