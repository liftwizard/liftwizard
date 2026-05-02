package com.example.helloworld.auth;

import javax.annotation.Nullable;
import javax.ws.rs.container.ContainerRequestContext;

import com.example.helloworld.core.User;
import io.dropwizard.auth.Authorizer;

public class ExampleAuthorizer implements Authorizer<User> {

	@Override
	public boolean authorize(User user, String role, @Nullable ContainerRequestContext requestContext) {
		return user.getRoles() != null && user.getRoles().contains(role);
	}
}
