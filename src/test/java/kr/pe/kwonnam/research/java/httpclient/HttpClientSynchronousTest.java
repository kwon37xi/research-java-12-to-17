package kr.pe.kwonnam.research.java.httpclient;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DisplayName("HttpClientSynchronousTest")
class HttpClientSynchronousTest {

    HttpClient httpClient;

    @BeforeEach
    void setUp() {
        httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(1))
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
}