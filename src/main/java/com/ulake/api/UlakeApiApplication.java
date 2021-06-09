package com.ulake.api;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.ulake.api.security.services.FilesStorageService;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;

@SpringBootApplication
public class UlakeApiApplication {
	@Resource
	FilesStorageService storageService;

	public static void main(String[] args) {
		SpringApplication.run(UlakeApiApplication.class, args);
	}

	@Bean
	public OpenAPI customOpenAPI(@Value("1.5.8") String appVersion) {
		var securitySchemeName = "bearer-key";
		return new OpenAPI()
				.components(new Components().addSecuritySchemes(securitySchemeName,
						new SecurityScheme().name(securitySchemeName).type(SecurityScheme.Type.HTTP).scheme("bearer")
								.bearerFormat("JWT")))
				.info(new Info().title("Lake API").version(appVersion)
						.license(new License().name("Apache 2.0").url("http://springdoc.org")));
	}

}
