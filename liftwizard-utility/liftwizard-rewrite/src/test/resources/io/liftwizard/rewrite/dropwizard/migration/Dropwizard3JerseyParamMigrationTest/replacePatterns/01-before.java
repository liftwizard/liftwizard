import io.dropwizard.jersey.params.BooleanParam;
import io.dropwizard.jersey.params.DateTimeParam;
import io.dropwizard.jersey.params.DurationParam;
import io.dropwizard.jersey.params.InstantParam;
import io.dropwizard.jersey.params.LocalDateParam;
import io.dropwizard.jersey.params.SizeParam;

class Test
{
	void packageMoves(InstantParam instant, LocalDateParam date)
	{
		Object a = instant.get();
		Object b = date.get();
	}

	void dateTimeParamReplacement(DateTimeParam dateTime)
	{
		Object value = dateTime.get();
	}

	void unwrapBooleanParam(BooleanParam flag)
	{
		Boolean value = flag.get();
	}

	void unwrapDurationParam(DurationParam duration)
	{
		Object value = duration.get();
	}

	void unwrapSizeParam(SizeParam size)
	{
		Object value = size.get();
	}
}
