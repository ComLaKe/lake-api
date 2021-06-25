package com.ulake.api.payload.request;

import java.util.List;

import javax.validation.constraints.NotBlank;

public class CreateFolderRequest {
	@NotBlank
	private String name;

	@NotBlank
	private String source;

	@NotBlank
	private List<String> topics;

	private String language;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public List<String> getTopics() {
		return topics;
	}

	public void setTopics(List<String> topics) {
		this.topics = topics;
	}
	
	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
}
