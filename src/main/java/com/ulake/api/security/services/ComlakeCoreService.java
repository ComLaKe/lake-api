package com.ulake.api.security.services;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ComlakeCoreService {
	@Value("${app.coreBasePath}")
	private String coreBasePath;

	private RestTemplate restTemplate = new RestTemplate();

	private HttpMessageConverter<?> jacksonSupportsMoreTypes() {
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setSupportedMediaTypes(Arrays.asList(MediaType.parseMediaType("text/plain;charset=utf-8"),
				MediaType.APPLICATION_OCTET_STREAM));
		return converter;
	}

	// TODO: POST /file
	public String postFile(byte[] data, Long size, String mimeType) throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.set("Content-Length", size.toString());
		headers.set("Content-Type", mimeType);

		HttpEntity<byte[]> entity = new HttpEntity<>(data, headers);

		ResponseEntity<byte[]> response = restTemplate.postForEntity(coreBasePath + "file", entity, byte[].class);

		// Get and save the response cid
		ObjectMapper mapperCreate = new ObjectMapper();
		JsonNode rootCreate = mapperCreate.readTree(response.getBody());
		String cid = rootCreate.path("cid").asText();
		return cid;
	}

	// TODO: POST /dir
	// TODO: GET /file/{cid}
	// TODO: POST /cp
	// TODO: GET /dir/{cid}
	// TODO: POST /add
	public String addDataset(String cid, String name, String source, List<String> topics, Long size, String mimeType,
			String language) throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

		JSONObject dataset = new JSONObject();
		dataset.put("file", cid);
		dataset.put("description", name);
		dataset.put("source", source);
		dataset.put("mimeType", mimeType);
		dataset.put("size", size);
		dataset.put("topics", new JSONArray(topics));
		if (language != null) {
			dataset.put("language", language);
		}
		
		HttpEntity<String> requestDataset = new HttpEntity<String>(dataset.toString(), headers);
		ResponseEntity<String> responseDataset = restTemplate.postForEntity(coreBasePath + "add", requestDataset,
				String.class);

		// Get and save the response datasetId
		ObjectMapper mapperDataset = new ObjectMapper();
		JsonNode rootDataset = mapperDataset.readTree(responseDataset.getBody());
		String datasetId = rootDataset.path("id").asText();

		return datasetId;
	}
	// TODO: POST /update
	// TODO: POST /find by datasetId

}
