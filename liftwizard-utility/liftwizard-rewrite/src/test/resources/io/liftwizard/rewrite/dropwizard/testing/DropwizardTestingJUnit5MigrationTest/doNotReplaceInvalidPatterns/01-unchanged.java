import io.dropwizard.testing.junit5.DropwizardClientExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import io.liftwizard.junit.extension.app.LiftwizardAppExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

@ExtendWith(DropwizardExtensionsSupport.class)
class MyTest {
	@RegisterExtension
	public static LiftwizardAppExtension<Object> APP_RULE =
			new LiftwizardAppExtension<>(Object.class, "config.yml");

	@RegisterExtension
	public static DropwizardClientExtension CLIENT_RULE =
			new DropwizardClientExtension(new Object());

	@RegisterExtension
	public static ResourceExtension RESOURCES = ResourceExtension.builder()
			.addResource(new Object())
			.build();
}
