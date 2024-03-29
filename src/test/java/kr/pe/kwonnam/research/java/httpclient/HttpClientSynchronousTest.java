package kr.pe.kwonnam.research.java.httpclient;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@DisplayName("HttpClientSynchronousTest")
class HttpClientSynchronousTest {

    HttpClient httpClient;

    @BeforeEach
    void setUp() {
        httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(3))
            .build();
    }

    @Test
    void get() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create("https://httpbin.org/get"))
            .setHeader("User-Agent", "Java 11 HttpClient Bot")
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        HttpHeaders headers = response.headers();
        log.info("Headers : {}", headers.map());

        assertThat(response.statusCode()).isEqualTo(200);
        log.info("Body : {}", response.body());

        DocumentContext ctx = JsonPath.parse(response.body());
        String url = ctx.read("$.url");

        assertThat(url).isEqualTo("https://httpbin.org/get");

        String userAgent = ctx.read("$.headers.User-Agent");
        assertThat(userAgent).isEqualTo("Java 11 HttpClient Bot");
    }

    @Test
    @DisplayName("POST 요청에 Form 파라미터 설정을 한다.")
    void postFormParameters() throws IOException, InterruptedException {
        Map<Object, Object> data= new HashMap<>();

        data.put("username", "abc");
        data.put("password", "123");
        data.put("custom", "한글과 English");
        data.put("ts", System.currentTimeMillis());

        HttpRequest request = HttpRequest.newBuilder()
            .POST(ofFormData(data))
            .uri(URI.create("https://httpbin.org/post"))
            .setHeader("User-Agent", "Java 11 HttpClient POST Bot")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        log.info("POST response body : {}", response.body());
        assertThat(response.statusCode()).isEqualTo(200);

        DocumentContext ctx = JsonPath.parse(response.body());

        String userAgentHeader = ctx.read("$.headers.User-Agent");

        assertThat(ctx.read("$.form.custom", String.class)).isEqualTo("한글과 English");
        assertThat(ctx.read("$.form.username", String.class)).isEqualTo("abc");
        assertThat(ctx.read("$.form.password", String.class)).isEqualTo("123");
        assertThat(ctx.read("$.headers.User-Agent", String.class)).isEqualTo("Java 11 HttpClient POST Bot");
    }

    private HttpRequest.BodyPublisher ofFormData(Map<Object, Object> data) {
        var builder = new StringBuilder();

        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }

    @Test
    @DisplayName("POST 요청 바디로 JSON 을 지정한다.")
    void postJsonBody() throws IOException, InterruptedException {
        String json = new StringBuilder()
            .append("{")
            .append("\"name\": \"권남\",")
            .append("\"notes\": \"hello world!\"")
            .append("}")
            .toString();

        HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .uri(URI.create("https://httpbin.org/post"))
            .setHeader("Usr-Agent", "Java 11 HttpClient POST json body bot")
            .setHeader("Content-Type", "application/json")
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        log.info("POST json response body : {}", response.body());
        assertThat(response.statusCode()).isEqualTo(200);
        DocumentContext ctx = JsonPath.parse(response.body());

        String data = ctx.read("$.data", String.class);
        log.info("POST json response data : {}", data);

        DocumentContext dataCtx = JsonPath.parse(data);
        assertThat(dataCtx.read("$.name", String.class)).isEqualTo("권남");
        assertThat(dataCtx.read("$.notes", String.class)).isEqualTo("hello world!");
    }

    @Test
    @DisplayName("BasicAuth 인증하기")
    void authenticate() throws IOException, InterruptedException {
        HttpClient authHttpClient = HttpClient.newBuilder()
            .authenticator(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("kwonnam", "thisismyPassw0rd!".toCharArray());
                }
            })
            .connectTimeout(Duration.ofSeconds(10))
            .build();

        HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create("https://httpbin.org/basic-auth/kwonnam/thisismyPassw0rd!"))
            .setHeader("User-Agent", "Java 11 HttpClient auth Bot")
            .build();

        HttpResponse<String> response = authHttpClient.send(request, HttpResponse.BodyHandlers.ofString());

        log.info("auth response body : {}", response.body());

        assertThat(response.statusCode()).isEqualTo(200);

        DocumentContext ctx = JsonPath.parse(response.body());

        assertThat(ctx.read("$.authenticated", Boolean.class)).isTrue();
        assertThat(ctx.read("$.user", String.class)).isEqualTo("kwonnam");
    }

    @Test
    @DisplayName("응답내용을 파일로 저장한다.")
    void responseBodyAsFile() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://httpbin.org/get?name=HttpClient&greetings=helloworld!"))
            .build();

        HttpResponse<Path> response =
            httpClient.send(request, HttpResponse.BodyHandlers.ofFile(Files.createTempFile("httpresponse", ".json")));

        log.info("response in file : {}", response.body());

        Path responsePath = response.body();
        String jsonStr = Files.readString(responsePath, StandardCharsets.UTF_8);

        DocumentContext ctx = JsonPath.parse(jsonStr);

        assertThat(ctx.read("$.args.greetings", String.class)).isEqualTo("helloworld!");
        assertThat(ctx.read("$.args.name", String.class)).isEqualTo("HttpClient");

        Files.deleteIfExists(responsePath);
    }
}