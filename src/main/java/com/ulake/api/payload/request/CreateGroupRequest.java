package com.ulake.api.payload.request;

import javax.validation.constraints.NotBlank;

public class CreateGroupRequest {
	@NotBlank
	private String name;

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

}
