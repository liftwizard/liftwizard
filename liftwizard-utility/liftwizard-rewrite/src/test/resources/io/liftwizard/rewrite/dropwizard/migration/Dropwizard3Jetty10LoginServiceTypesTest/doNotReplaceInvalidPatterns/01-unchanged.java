class UnrelatedTypes {

	static class UserPrincipal {
		UserPrincipal(String name) {}
	}

	static class RolePrincipal {
		RolePrincipal(String name) {}
	}

	UserPrincipal getUser() {
		return new UserPrincipal("alice");
	}

	RolePrincipal getRole() {
		return new RolePrincipal("admin");
	}
}
