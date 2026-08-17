import org.eclipse.jetty.security.AbstractLoginService;
import org.eclipse.jetty.security.UserPrincipal;

class AdminLoginService
	extends AbstractLoginService
{
	private final String adminUserName;
	private final UserPrincipal adminPrincipal;

	AdminLoginService(String userName)
	{
		this.adminUserName = userName;
		this.adminPrincipal = new UserPrincipal(userName, "secret");
	}

	@Override
	protected String[] loadRoleInfo(UserPrincipal principal)
	{
		if (this.adminUserName.equals(principal.getName()))
		{
			return new String[]
			{
				"admin",
			};
		}
		if ("multi".equals(principal.getName()))
		{
			return new String[]
			{
				"admin",
				"ops",
			};
		}
		return new String[0];
	}

	@Override
	protected UserPrincipal loadUserInfo(String userName)
	{
		return this.adminUserName.equals(userName) ? this.adminPrincipal : null;
	}
}
