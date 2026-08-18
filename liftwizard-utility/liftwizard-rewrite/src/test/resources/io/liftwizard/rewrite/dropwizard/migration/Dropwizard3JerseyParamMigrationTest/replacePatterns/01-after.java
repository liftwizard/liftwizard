import java.time.Duration;

import io.dropwizard.jersey.jsr310.InstantParam;
import io.dropwizard.jersey.jsr310.LocalDateParam;
import io.dropwizard.jersey.jsr310.ZonedDateTimeParam;
import io.dropwizard.util.DataSize;

class Test
{
	void packageMoves(InstantParam instant, LocalDateParam date)
	{
		Object a = instant.get();
		Object b = date.get();
	}

	void dateTimeParamReplacement(ZonedDateTimeParam dateTime)
	{
		Object value = dateTime.get();
	}

	void unwrapBooleanParam(Boolean flag)
	{
		Boolean value = flag;
	}

	void unwrapDurationParam(Duration duration)
	{
		Object value = duration;
	}

	void unwrapSizeParam(DataSize size)
	{
		Object value = size;
	}
}
