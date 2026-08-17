import org.eclipse.collections.impl.utility.ArrayIterate;

class Test
{
	void test(String[] strings, Object[] objects)
	{
		boolean isEmptyNullOrLength = strings == null || strings.length == 0;
		boolean isEmptyNullOrLengthLessOrEqual = strings == null || strings.length <= 0;
		boolean isEmptyNullOrLengthLessThanOne = strings == null || strings.length < 1;
		boolean notEmptyNotNullAndLength = objects != null && objects.length > 0;
		boolean notEmptyNotNullAndLengthNotEqual = objects != null && objects.length != 0;
		boolean notEmptyNotNullAndLengthGreaterOrEqual = objects != null && objects.length >= 1;

		if (strings == null || strings.length == 0)
		{
		}

		if (strings == null || strings.length <= 0)
		{
		}

		if (strings == null || strings.length < 1)
		{
		}

		if (objects != null && objects.length > 0)
		{
		}

		if (objects != null && objects.length != 0)
		{
		}

		if (objects != null && objects.length >= 1)
		{
		}
	}

	boolean testNegatedArrayIterateIsEmpty(String[] array)
	{
		return !ArrayIterate.isEmpty(array);
	}

	boolean testNegatedArrayIterateNotEmpty(String[] array)
	{
		return !ArrayIterate.notEmpty(array);
	}

	void testMultipleNegated(String[] array1, Object[] array2)
	{
		if (!ArrayIterate.isEmpty(array1))
		{
		}

		if (!ArrayIterate.notEmpty(array2))
		{
		}
	}
}
