import java.util.List;

import org.eclipse.jetty.security.AbstractLoginService;
import org.eclipse.jetty.security.RolePrincipal;
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
	protected List<RolePrincipal> loadRoleInfo(UserPrincipal principal)
	{
		if (this.adminUserName.equals(principal.getName()))
		{
			return List.of(new RolePrincipal("admin"));
		}
		if ("multi".equals(principal.getName()))
		{
			return List.of(new RolePrincipal("admin"), new RolePrincipal("ops"));
		}
		return List.of();
	}

	@Override
	protected UserPrincipal loadUserInfo(String userName)
	{
		return this.adminUserName.equals(userName) ? this.adminPrincipal : null;
	}
}
