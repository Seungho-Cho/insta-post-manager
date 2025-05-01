package com.plamason.postmanager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class PostManagerApplicationTests {

	@Autowired
	private RestTemplate restTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	void postTset() {

	}

	@Test
	public void testAccessTokenValidation() {
		// 테스트용 토큰 (교체 필요)
		String inputToken = "IGAATqvZBxo6fpBZAE1relpCeEVfdUpTMGV5SnktWjVUR1EzWF8xb3hRdXFveVpib2pXM2RCcXhvcEhvRVhKYXF3SFYwczg3N3VOM2FUNm9DeUM2dDdqNlhuN3lvMU10QlFZAeHhPVS1XbDVzUTRzenZADN2RlZA05CYTA4UmJnNXlQSQZDZD";  // 검증할 토큰
		String appId = "1384009932794362";
		String appSecret = "807969d65f12291643f8e7505f8d0047";
		String appAccessToken = appId + "|" + appSecret;

		// 요청 URL 구성
		String url = UriComponentsBuilder.fromHttpUrl("https://graph.facebook.com/v22.0/debug_token")
				.queryParam("input_token", inputToken)
				//.queryParam("access_token", appAccessToken)
				.toUriString();

		try {
			// API 호출
			ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

			// 응답을 검사 (HTTP 상태 코드가 2xx인지 확인)
			assertNotNull(response);
			assertTrue(response.getStatusCode().is2xxSuccessful());

			// JSON 응답 로깅 (응답 본문만 출력)
			String responseBody = response.getBody();
			if (responseBody != null) {
				System.out.println("Response Body: " + responseBody);  // JSON 정보 로깅
			}

			// 응답에 유효성 여부가 포함되어 있으면 이를 로깅 (토큰이 유효하지 않아도 응답은 성공으로 간주)
			assertTrue(responseBody.contains("\"is_valid\":"));
		} catch (HttpClientErrorException e) {
			// 예외 처리 (HTTP 상태 코드가 2xx가 아니면 실패 처리)
			System.err.println("Error response: " + e.getResponseBodyAsString());
			fail("Error during token validation: " + e.getMessage());
		}
	}



	@Test
	public void testInstagramUploadAndPublish() {
		String userId = "17841425534731357";
		String accessToken = "IGAATqvZBxo6fpBZAE1relpCeEVfdUpTMGV5SnktWjVUR1EzWF8xb3hRdXFveVpib2pXM2RCcXhvcEhvRVhKYXF3SFYwczg3N3VOM2FUNm9DeUM2dDdqNlhuN3lvMU10QlFZAeHhPVS1XbDVzUTRzenZADN2RlZA05CYTA4UmJnNXlQSQZDZD";

		// Step 1: Create media container
		String mediaUrl = "https://graph.instagram.com/v22.0/" + userId + "/media";
		Map<String, Object> mediaBody = new HashMap<>();
		mediaBody.put("image_url", "https://upload.wikimedia.org/wikipedia/commons/9/9a/Gull_portrait_ca_usa.jpg");
		mediaBody.put("caption", "Test Post");
		mediaBody.put("domain", "INSTAGRAM");
		mediaBody.put("access_token", accessToken);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Map<String, Object>> mediaRequest = new HttpEntity<>(mediaBody, headers);
		String creationId = null;

		try {
			ResponseEntity<Map> response = restTemplate.postForEntity(mediaUrl, mediaRequest, Map.class);
			System.out.println("Media creation response: " + response.getBody());
			assertTrue(response.getStatusCode().is2xxSuccessful());

			// 컨테이너 ID 획득
			creationId = (String) response.getBody().get("id");
			assertTrue(creationId != null && !creationId.isEmpty());

		} catch (HttpStatusCodeException e) {
			System.out.println("Media creation error: " + e.getResponseBodyAsString());
			assertTrue(false, "Media creation failed");
		}

		// Step 2: Publish the media container
		String publishUrl = "https://graph.instagram.com/v22.0/" + userId + "/media_publish";
		Map<String, Object> publishBody = new HashMap<>();
		publishBody.put("creation_id", creationId);
		publishBody.put("access_token", accessToken);

		HttpEntity<Map<String, Object>> publishRequest = new HttpEntity<>(publishBody, headers);

		try {
			ResponseEntity<String> publishResponse = restTemplate.postForEntity(publishUrl, publishRequest, String.class);
			System.out.println("Publish response: " + publishResponse.getBody());
			assertTrue(publishResponse.getStatusCode().is2xxSuccessful());
		} catch (HttpStatusCodeException e) {
			System.out.println("Publish error: " + e.getResponseBodyAsString());
			assertTrue(false, "Media publish failed");
		}
	}
}
