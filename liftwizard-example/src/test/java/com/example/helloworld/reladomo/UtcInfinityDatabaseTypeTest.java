package com.example.helloworld.reladomo;

import java.util.TimeZone;

import com.gs.fw.common.mithra.util.MithraTimestamp;
import io.liftwizard.reladomo.test.extension.ReladomoInitializeExtension;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Tests the UTC infinity database type on its own.
 *
 * <p>{@code H2InMemoryConnectionManager} uses {@code UtcInfinityH2DatabaseType}, and Reladomo's default time zone is
 * reset to the JVM default time zone, so the database type is the only mechanism in effect.</p>
 */
class UtcInfinityDatabaseTypeTest
	extends AbstractReladomoInfinityTest
{
	@RegisterExtension
	@Order(2)
	final ReladomoInitializeExtension initializeExtension = new ReladomoInitializeExtension(
		"reladomo-runtime-configuration/H2InMemoryReladomoRuntimeConfiguration.xml"
	);

	@RegisterExtension
	@Order(3)
	final BeforeEachCallback useJvmDefaultTimeZoneInReladomo = (context) ->
		MithraTimestamp.zSetDefaultTimezone(TimeZone.getDefault());
}
