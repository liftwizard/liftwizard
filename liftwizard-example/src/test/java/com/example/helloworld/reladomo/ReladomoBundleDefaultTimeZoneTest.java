package com.example.helloworld.reladomo;

import java.sql.Timestamp;
import java.util.TimeZone;

import com.example.helloworld.HelloWorldApplication;
import com.example.helloworld.HelloWorldConfiguration;
import com.gs.fw.common.mithra.util.MithraTimestamp;
import io.dropwizard.testing.ResourceHelpers;
import io.liftwizard.junit.extension.app.LiftwizardAppExtension;
import io.liftwizard.reladomo.utc.infinity.timestamp.UtcInfinityTimestamp;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code ReladomoBundle} sets Reladomo's default time zone to UTC, so Reladomo binds as-of infinity as its UTC wall
 * clock even with Reladomo's own database types.
 */
class ReladomoBundleDefaultTimeZoneTest
{
	private static final String CONFIG_PATH = ResourceHelpers.resourceFilePath("test-example.json5");

	@RegisterExtension
	@Order(0)
	final BeforeEachCallback useJvmDefaultTimeZoneInReladomo = (context) ->
		MithraTimestamp.zSetDefaultTimezone(TimeZone.getDefault());

	@RegisterExtension
	@Order(1)
	final LiftwizardAppExtension<HelloWorldConfiguration> dropwizardAppExtension = new LiftwizardAppExtension<>(
		HelloWorldApplication.class,
		CONFIG_PATH
	);

	@Test
	void bindsInfinityAsUtcWallClock()
	{
		Timestamp bound = MithraTimestamp.zConvertTimeForWritingWithUtcCalendar(
			UtcInfinityTimestamp.getDefaultInfinity(),
			MithraTimestamp.DefaultTimeZone
		);

		assertThat(bound.toInstant()).isEqualTo(UtcInfinityTimestamp.getDefaultInfinityInstant());
	}
}
