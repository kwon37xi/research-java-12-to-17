package kr.pe.kwonnam.research.java.completablefuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import java.time.Duration;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.google.common.base.Stopwatch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JustAsFutureTest {
    private JustAsFuture justAsFuture;

    @BeforeEach
    void setUp() {
        justAsFuture = new JustAsFuture();
    }

    @Test
    @DisplayName("쓰레드 풀에서 실행한 결과 반환")
    void calculateAsync() throws InterruptedException, ExecutionException {
        Stopwatch stopwatch = Stopwatch.createStarted();

        Future<String> future = justAsFuture.calculateAsync();
        Duration beforeGet = stopwatch.elapsed();

        assertThat(beforeGet).as("CompletableFuture 생성에는 시간이 거의 들지 않는다.")
            .isBetween(Duration.ofMillis(0), Duration.ofMillis(5));

        String result = future.get();
        stopwatch.stop();
        Duration totalElapsed = stopwatch.elapsed();

        log.info("before get : {}ms, after get : {}ms", beforeGet.toMillis(), totalElapsed.toMillis());
        assertThat(totalElapsed).as("get 내부에서 500ms sleep 하므로 500ms 이상 걸려야 한다.")
            .isBetween(Duration.ofMillis(500), Duration.ofMillis(510));
        assertThat(result).isEqualTo("hello");

    }

    @Test
    @DisplayName("completeFuture 로 이미 결정된 정적 데이터 반환")
    void completedFuture() throws ExecutionException, InterruptedException {
        Future<String> future = CompletableFuture.completedFuture("World!");

        String result = future.get();

        assertThat(result).isEqualTo("World!");
    }

    @Test
    void calculateAsyncWithCancellation() {
        CancellationException cancellationException = catchThrowableOfType(() -> {
                Future<String> future = justAsFuture.calculateAsyncWithCancellation();
                future.get();
            },
            CancellationException.class);

        assertThat(cancellationException)
            .hasMessage(null)
            .hasNoCause();
    }
}
