import org.eclipse.jetty.security.AbstractLoginService;
import org.eclipse.jetty.security.RolePrincipal;
import org.eclipse.jetty.security.UserPrincipal;

class AdminLoginService
	extends AbstractLoginService
{
	private final UserPrincipal adminPrincipal = new UserPrincipal("admin", "secret");

	String[] loadRoleInfo(UserPrincipal principal)
	{
		return new String[]
		{
			"admin",
		};
	}

	UserPrincipal loadUserInfo(String userName)
	{
		return this.adminPrincipal;
	}

	RolePrincipal makeRole(String name)
	{
		return new RolePrincipal(name);
	}
}
