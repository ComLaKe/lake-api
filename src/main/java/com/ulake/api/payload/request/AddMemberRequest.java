package com.ulake.api.payload.request;

import java.util.Set;

public class AddMemberRequest {
	private Set<String> user;

	public Set<String> getUser() {
		return this.user;
	}

	public void setUser(Set<String> user) {
		this.user = user;
	}
}
