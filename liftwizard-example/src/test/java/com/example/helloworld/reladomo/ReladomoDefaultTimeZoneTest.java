package com.example.helloworld.reladomo;

import io.liftwizard.reladomo.test.extension.ReladomoInitializeExtension;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Tests setting Reladomo's default time zone to UTC on its own.
 *
 * <p>{@link ReladomoInitializeExtension} sets Reladomo's default time zone to UTC, like {@code ReladomoBundle} does.
 * {@link H2DatabaseTypeConnectionManager} uses Reladomo's own {@code H2DatabaseType}, so the default time zone is the
 * only mechanism in effect.</p>
 */
class ReladomoDefaultTimeZoneTest
	extends AbstractReladomoInfinityTest
{
	@RegisterExtension
	@Order(2)
	final ReladomoInitializeExtension initializeExtension = new ReladomoInitializeExtension(
		"reladomo-runtime-configuration/H2DatabaseTypeReladomoRuntimeConfiguration.xml"
	);
}
