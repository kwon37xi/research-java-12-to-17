# CompletableFuture research
* package  [test kr/pe/kwonnam/research/java/completablefuture](src/main/java/kr/pe/kwonnam/research/java/completablefuture)
* [JustAsFutureTest](src/test/java/kr/pe/kwonnam/research/java/completablefuture/JustAsFutureTest)
* [CompletableFutureAsyncTest/java](src/test/java/kr/pe/kwonnam/research/java/completablefuture/CompletableFutureAsyncTest/java)
* [CompletableFutureJava9Test/java](src/test/java/kr/pe/kwonnam/research/java/completablefuture/CompletableFutureJava9Test/java)

* https://kwonnam/pe/kr/wiki/java/8/completable_future
* [Java 11 CompletableFuture](https://docs/oracle/com/en/java/javase/11/docs/api/java/base/java/util/concurrent/CompletableFuture/html)

## 참고 자료
* [Java 8: Definitive guide to CompletableFuture](https://www/nurkiewicz/com/2013/05/java-8-definitive-guide-to/html)
* [Java 8: CompletableFuture in action](https://www/nurkiewicz/com/2013/05/java-8-completablefuture-in-action/html)
* [CompletableFuture can't be interrupted](https://www/nurkiewicz/com/2015/03/completablefuture-cant-be-interrupted/html)
* [Asynchronous timeouts with CompletableFuture](https://www/nurkiewicz/com/2014/12/asynchronous-timeouts-with/html)
* [Promises and CompletableFuture](https://www/nurkiewicz/com/2013/12/promises-and-completablefuture/html)
* [Which thread executes CompletableFuture's tasks and callbacks?](https://www/nurkiewicz/com/2015/11/which-thread-executes/html)
* [Guide To CompletableFuture](https://www/baeldung/com/java-completablefuture)
* [Java 9 CompletableFuture API Improvements](https://www/baeldung/com/java-9-completablefuture)
* JustAsFuture/java, JustAsFutureTest/java, CompletableFutureAsyncTest/java

## thenApply
* `thenApply`는 `Stream/map`과 유사하게 `CompletableFuture`의 결과를 받아서 일반적인 다른 결과 값을 도출한다.
* `thenApply`의 인자는 앞선 CF 의 결과값이다.
* blocking 이 발생하지 않는다.

## thenCompose
* `thenCompose` 는 `Stream/flatMap` 과 유사하게, `CompletableFuture`의 결과를 받아서 다시 `CompletableFuture`를 생성한다.
* 일반적으로 비동기 작업의 연쇄가 계속 이뤄질경우에는 `thenCompose`를 주로 사용하게 된다.
* blocking 이 발생하지 않는다.

## thenCombine
* 두 개의 `CompletableFuture`를 독립 실행하고, 둘 다 실행이 끝나면 그 결과를 하나로 합치고자 할 때 사용한다.

## thenAcceptBoth, runAfterBoth
* `thenCombine`과 유사하지만, 결과 반환할게 없을 때 사용.
* 그 뒤로 할게 없는데도 `get()`을 사용하지 않고 `thenXXX`를 사용하는 것은 이들은 non-blocking 이기 때문이다.
`get()`은 쓰레드를 blocking 해버린다.

## acceptEither, runAfterEither
* 둘 중에 첫번째로 실행된 것의 결과만을 취해서 처리한다/ 반환할 게 없을 때 사용한다.

## applyToEither
* `acceptEight`, `runAfterEighter` 와 같이 첫번째로 실행된 것의 결과를 받아 다른 값으로 전환한다.
* 헌데, 사실 다른 값으로 바꿀 필요 없이, 첫번째 결과를 바로 리턴하는 역할만 해도 충분하다.
이 때는 `Function/identity()`로, 변경없이 반환하게 하고, 그 뒤여 변경(`map`) 처리가 필요하면 `thenApply`를 호출하면 된다.

## allOf
* 여러 `CompletableFuture`를 동시에 실행하고 그 결과를 모두 합친 `CompletableFuture` 를 생성한다.
* 단, `CompletableFuture<Void>` 를 리턴하기 때문에 실제 실행된 결과 값을 반환 받을 수 없다.

## allOf / thenCombine으로 여러 CompletableFuture의 결과를 조합해서 원하는 타입으로 리턴하는 방법
* [Java 8: CompletableFuture in action](https://www/nurkiewicz/com/2013/05/java-8-completablefuture-in-action/html)
* [CompletableFutureTypedAllOfTest.java](/src/test/java/kr/pe/kwonnam/research/java/completablefuture/CompletableFutureTypedAllOfTest.java)
* [CompletableFutureCollector.java Stream Collector](src/main/java/kr/pe/kwonnam/research/java/completablefuture/CompletableFutureCollector.java) : `allOf` 기반 collector
* [CompletableFutureCombineCollector.java Stream Collector](src/main/java/kr/pe/kwonnam/research/java/completablefuture/CompletableFutureCombineCollector.java) : `thenCombine` 기반 collector

## anyOf
* 여러 `CompletableFuture`를 동시에 실행하고 그 중에 가장 먼저 실행이 완료된 것을 반환한다.
* 단, `CompletableFuture<Object>` 타입이기 때문에, 어느 타입의 결과인지 명확히 알기 힘들다.

## join
* join 은 blocking 으로 결과가 나올 때 까지 기다려서, 그 결과를 리턴한다.
* 여러 CF를 동시 실행하고 모두 실행될 때까지 기다려서 그 결과를 합치고자 할때는 `Stream`, `join`을 사용한다.
* join 은 Unchecked Exception 만 던진다.

## get
* 결과가 나올 때 까지 기다려서, 그 결과를 리턴한다.
* Checked Exception 을 던진다.

```
String combined = Stream/of(future1, future2, future3)
  /map(CompletableFuture::join)
  /collect(Collectors/joining(" "));

assertEquals("Hello Beautiful World", combined);
```
* `CompletableFuture/join` 메소드는 `get`과 유사하지만, 해당 Future가 비정상 종료될 경우 Unchecked Exception 을 던진다(`get`은 Checked Exception).
Unchecked Exception 이라서 `Stream/map` 에서 사용가능하다.

## *Async
* `thenApplyAsync` 등 `*Async` 메소드들은 별도의 쓰레드 풀에서 해당 작업을 수행한다.
* 기본적으로 `ForkJoinPool.commonPool()`을 사용하는데, 이는 피하고, 명확하게 `Executor`를 지정해줘야한다.
쓰레드풀의 명확한 설정값을 모르는 상태로 기본 `ForkJoinPool`을 사용하다가 장애가 나는 경우가 있다.
항상 스스로 컨트롤 가능한 쓰레드 풀을 직접 만들어서 사용해야 한다.

## supplyAsync과 후속 thenApply, thenAccept 의 쓰레드
* [java - In which thread does CompletableFuture's completion handlers execute in? - Stack Overflow](https://stackoverflow/com/questions/46060438/in-which-thread-does-completablefutures-completion-handlers-execute-in)
* [Which thread executes CompletableFuture's tasks and callbacks?](https://www.nurkiewicz.com/2015/11/which-thread-executes.html)
* 작업의 `complete()` 시점 이전 혹은 이후에 `thenXXX` 호출 여부에 따라 `thenXXX` 의 실행 쓰레드가 달라질 수 있다.
* [CompletableFutureAsyncThreadPoolTest](src/test/java/kr/pe/kwonnam/research/java/completablefuture/CompletableFutureAsyncThreadPoolTest.java)
* 첫번째 `supplyAsync`(혹은 어쨌든 비동기 `CompletableFuture`)가 끝나기 전에 `thenApply`가
호출되면(그 안의 lambda가 실행된게 아니로 작업 등록만 된 것임 ) 이 작업은 앞선 `supplyAsync`와 동일한 쓰레드 풀에서 실행된다.
* 첫번째 `supplyAsync`(혹은 어쨌든 비동기 `CompletableFuture`)가 끝난 뒤에 `thenApply`가 호출되면 이 작업은
앞선 호출자 쓰레드(보통은 `main`)에서 실행된다.
* 만약 `thenApply`에서 오래걸리는 blocking 작업을 하면 해당 작업을 수행하는 쓰레드가 블로킹 된다.
* 따라서 완전 non blocking 코드를 짜야하는 경우거나, 어쨌든 비동기 작업을 원하는 경우에
 **확실하게** 별도 쓰레드에서 실행되길 권한다면 `thenApplyAsync` 같이 `*Async` 계통
메소드를 호출해야 한다.

## CompletableFuture 의 상속
* java 8의 `CompletableFuture` 는 사실상 상속하기 매우 어렵다.
* java 9에서는 `newIncompleteFuture`와 `completeAsync` 를 사용하여 조금 쉬워졌다.
* 상속시에 무조건 `CompletableFuture<U> newIncompleteFuture()`도 override 해야한다.
* 최초 시작지점이 보통 static 메소드인 `CompletableFuture/supplyAsync` 등일 가능성이 높은데,
이런 static 메소드들 까지 모두 새로 만들지 않으면 사실상 상속의 의미가 없다.
* 따라서 `CompletableFuture/defaultExecutor()`도 거의 읽기만 가능할 뿐, 이것만 override하는 것은 큰 의미가 없다.
* [Java 9 이후의 CompletableFuture의 상속 - completeAsync 사용](https://stackoverflow/com/a/56356109/1051402)
* [Java 8 이하의 CompletableFuture의 상속](https://stackoverflow/com/a/26607433/1051402)
* [NeoCompletableFuture - supplyAsync까지 모두 Override 예제](src/main/java/kr/pe/kwonnam/research/java/completablefuture/NeoCompletableFuture/java)

## copy()
* `copy()' 는 원본 `CompletableFuture`의 상태를 기본적으로 그대로 복제한다.
* 하지만 아직 원본의 작업이 끝나지 않은 상태를 복제한 뒤에 변경을 복제본에 가하면(`cancel() 등`) 그 여파는 복제본에만 반영된다.
* `CompletableFuture`를 반환하되, 해당 객체의 상태를 클라이언트가 변경할 수 없게 하고자 할 때 사용하면 될 듯.

## thenCompose, whenComplete, handle 의 차이점
* 셋 다 새로운 `CompletableFuture`를 리턴한다.
* `thenComplete` 는 앞선 실행이 오류가 없을 경우에만 후속으로 실행된다. `Stream/flatMap`과 유사하다.
* `whenComplete`, `handle` 은 앞선 실행이 성공으로 끝나든 예외가 발생하든 실행되면 성공값과 예외를 모두 인자로 받는다.
* `whenComplete` 는 그 자체로 종결되는 것이 목표다. `whenComplete`의 `supplier` 에서는 예외를 던지지 말아야한다.
* `handle` 은 다시 새로운 `CompletableFuture` 작업이 시작된다.

## whenComplete, handle 의 예외처리
* [Java CompletableFuture - Understanding CompletionStage.whenComplete() method](https://www.logicbig.com/tutorials/core-java-tutorial/java-multi-threading/completion-stage-when-complete.html)
* [Java CompletableFuture - Exception Handling](https://www.logicbig.com/tutorials/core-java-tutorial/java-multi-threading/completion-stages-exception-handling.html)
* [Java's CompleteableFuture exception handling: whenComplete vs. handle](https://dempkow.ski/blog/java-completablefuture-exception-handling/)
* [3 Ways to Handle Exception In Completable Future | Mincong's Blog](https://mincong.io/2020/05/30/exception-handling-in-completable-future/)

* `exceptionally` 는 앞선 `CompletionStage`에서 예외가 발생했을 때만 호출되며, 예외가 발생되지 않았으면 무시된다. 예외를 처리해서 정상상태로 보정한다.

* `whenComplete` 은 단순히 현재 상태의 실행 결과값과 예외 발생시 예외 값을 **볼 수만** 있다.
그리고 그 상태 그대로 후속 `CompletionStage`로 전달된다.
  * [CompletableFutureWhenCompleteTest.java](src/test/java/kr/pe/kwonnam/research/java/completablefuture/CompletableFutureWhenCompleteTest.java)
  * 즉, 앞선 stage 에서 예외가 발생했다면 `whenComplete` 을 거쳐도 계속 예외 발생 상태로 남아있다.
  * 따라서 `whenComplete`에서 절대로 예외가 발생하지 않게 주의해야한다.
  * 만약 `whenComplete`에서 예외가 발생한다면,
    * 앞선 작업에서 예외가 발생했다면 `whenComplete`의 예외는 **무시되고** 앞선 작업의 예외가 후속 `CompletionStage`로 전달된다.
    * 앞선 작업이 정상 종료됐다면 `whenComplete`의 예외가 후속 `CompletionStage`로 전달된다.
* `handle` 은 현재 상태의 실행 결과값과 예외 발생시의 예외 값을 받아서 원하는 대로 처리하고 후속 `CompletionStage` 로 변환해서 넘길 수 있다.
  * 즉, 앞선 stage 에서 예외가 발생한 것을 `handle` 에서 정상 상태로 보정하는 것도 가능하고,
  * 여기서 예외가 발생하면 그 예외가 후속 `CompletionStage`로 전달된다.

## orTimeout
* 시간내에 complete 되지 않을 경우 `TimeoutException`이 발생하고, 시간내에 완료되면 그대로 결과를 반환한다.

## completeOnTimeout
* 시간내에 complete 되지 않을 경우 `completeOnTimeout`에 지정된 값이 결과로 반환되고, 성공한 `CompletableFuture`로 간주된다.

## delayedExecutor
* static 메소드로, 기반 executor를 무조건 지정된 시간만큼 지연을 시켜서 submit 이 되게끔 해주는 감싸진 executor 를 반환한다.
