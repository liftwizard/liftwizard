import io.dropwizard.testing.junit.DropwizardAppRule;
import io.dropwizard.testing.junit.DropwizardClientRule;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.ClassRule;
import org.junit.Rule;

class MyTest
{
	@ClassRule
	public static DropwizardAppRule<Object> APP_RULE = new DropwizardAppRule<>(Object.class, "config.yml");

	@Rule
	public DropwizardAppRule<Object> instanceRule = new DropwizardAppRule<>(Object.class, "config.yml");

	@ClassRule
	public static DropwizardClientRule CLIENT_RULE = new DropwizardClientRule(new Object());

	@ClassRule
	public static ResourceTestRule RESOURCES = ResourceTestRule.builder().addResource(new Object()).build();
}
