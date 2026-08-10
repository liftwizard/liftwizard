import org.eclipse.jetty.security.AbstractLoginService;
import org.eclipse.jetty.security.UserPrincipal;

class Sibling {

	// Same name but unrelated containing class — leave alone since UsesType
	// only fires for the AbstractLoginService-using compilation unit; this
	// covers the case where loadRoleInfo lives next to one but isn't an
	// override.
	String[] loadRoleInfo(String unrelated) {
		return new String[] { unrelated };
	}

	// Different return type — already migrated, no-op.
	java.util.List<String> alreadyMigrated(UserPrincipal principal) {
		return java.util.List.of(principal.getName());
	}

	// Two-arg overload — wrong arity, leave alone.
	String[] loadRoleInfo(UserPrincipal principal, int extra) {
		return new String[0];
	}

	// String[] return value that has nothing to do with loadRoleInfo —
	// since it's not inside a target method, must not be rewritten.
	String[] unrelated() {
		return new String[] { "keep" };
	}

	// Force the AbstractLoginService UsesType precondition to fire so the
	// visitor actually traverses this CU.
	AbstractLoginService anchor() {
		return null;
	}
}
