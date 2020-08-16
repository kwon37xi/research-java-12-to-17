package kr.pe.kwonnam.research.java.httpclient;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class HttpClientAsynchronousTest {
    static ExecutorService executor;
    HttpClient httpClient;

    @BeforeAll
    static void beforeAll() {
        // 비동기 요청이 이 쓰레드 풀을 사용하는 것이 아님을 증명하기 위해 쓰레드 갯수를 1로 고정함.
        executor = Executors.newFixedThreadPool(1);
    }

    @AfterAll
    static void afterAll() {
        executor.shutdownNow();
    }

    @BeforeEach
    void setUp() {
        httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(1))
            .executor(executor)
            .build();
    }

    @Test
    void delay() throws InterruptedException, ExecutionException, TimeoutException {
        var start = Instant.now();

        List<CompletableFuture<HttpResponse<String>>> delayedFutures =
            List.of(delayedRequest(1),
                delayedRequest(3),
                delayedRequest(5),
                delayedRequest(4),
                delayedRequest(5));

        var afterFutureGeneratedDuration = Duration.between(start, Instant.now());

        List<String> bodies = delayedFutures.stream()
            .map(CompletableFuture::join)
            .map(HttpResponse::body)
            .collect(toUnmodifiableList());

        var finishedDuration = Duration.between(start, Instant.now());

        log.info("Async durations - afterFutureGenerated : {}ms, finished : {}ms",
            afterFutureGeneratedDuration.toMillis(), finishedDuration.toMillis());

        assertThat(afterFutureGeneratedDuration)
            .as("비동기 이므로 모든 요청 생성은 시간이 매우 짧게 걸린다. 커넥션이 오래걸릴경우 가끔 실패할수도 있다.")
            .isBetween(Duration.ofMillis(0), Duration.ofMillis(1000));

        assertThat(finishedDuration)
            .as("비동기이므로 모든 요청시간의 총 합이 아니라, 가장 오래 걸리는 요청 수준의 시간만 걸린다.")
            .isBetween(Duration.ofSeconds(5), Duration.ofSeconds(10));

        bodies.stream().forEach(body -> {
            log.info("Body : {}", body);

            DocumentContext ctx = JsonPath.parse(body);
            String url = ctx.read("$.url");

            assertThat(url).startsWith("https://httpbin.org/delay");

            String userAgent = ctx.read("$.headers.User-Agent");
            assertThat(userAgent).isEqualTo("Java 11 HttpClient Async Bot");
        });
    }

    private CompletableFuture<HttpResponse<String>> delayedRequest(long seconds) {
        HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create("https://httpbin.org/delay/" + seconds))
            .setHeader("User-Agent", "Java 11 HttpClient Async Bot")
            .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    @DisplayName("stream 을 이용해 비동기 동시 호출 예제 - https://openjdk.java.net/groups/net/httpclient/recipes.html")
    void concurrentRequests() throws ExecutionException, InterruptedException {
        CompletableFuture<List<String>> resultCompletableFuture = getFromURIs(List.of(
            URI.create("https://httpbin.org/get?greetings=hello"),
            URI.create("https://httpbin.org/get?greetings=world"),
            URI.create("https://httpbin.org/get?greetings=안녕"),
            URI.create("https://httpbin.org/get?greetings=세상아")
        ));

        List<String> results = resultCompletableFuture.get();

        log.info("concurrentRequests results : {}", results);
        assertThat(results)
            .isNotEmpty();
    }

    public CompletableFuture<List<String>> getFromURIs(List<URI> uris) {
        List<HttpRequest> requests = uris.stream()
            .map(HttpRequest::newBuilder)
            .map(reqBuilder -> reqBuilder.build())
            .collect(toList());

        CompletableFuture<HttpResponse<String>>[] completableFutures = requests.stream()
            .map(request -> httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()))
            .toArray(CompletableFuture[]::new);

        CompletableFuture<List<String>> resultCompletableFuture = CompletableFuture.allOf(completableFutures)
            .thenApply(unused -> Stream.of(completableFutures)
                .map(CompletableFuture::join)
                .map(HttpResponse::body)
                .collect(toList()));
        return resultCompletableFuture;
    }
}
