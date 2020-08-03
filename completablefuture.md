# CompletableFuture research
* package `kr.pe.kwonnam.research.java.completablefuture`
* https://kwonnam.pe.kr/wiki/java/8/completable_future
* [Java 11 CompletableFuture](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/CompletableFuture.html)

## Baeldung Guide To CompletableFuture
* [Guide To CompletableFuture](https://www.baeldung.com/java-completablefuture)
* [Java 9 CompletableFuture API Improvements](https://www.baeldung.com/java-9-completablefuture)
* JustAsFuture.java, JustAsFutureTest.java, CompletableFutureAsyncTest.java

### thenApply
* `thenApply`는 `Stream.map`과 유사하게 `CompletableFuture`의 결과를 받아서 일반적인 다른 결과 값을 도출한다.
* `thenApply`의 인자는 앞선 CF 의 결과값이다.

### thenCompose
* `thenCompose` 는 `Stream.flatMap` 과 유사하게, `CompletableFuture`의 결과를 받아서 다시 `CompletableFuture`를 생성한다.
* 일반적으로 비동기 작업의 연쇄가 계속 이뤄질경우에는 `thenCompose`를 주로 사용하게 된다.

### thenCombine
* 두 개의 `CompletableFuture`를 독립 실행하고, 둘 다 실행이 끝나면 그 결과를 하나로 합치고자 할 때 사용한다.

### allOf
* 여러 `CompletableFuture`를 동시에 실행하고 그 결과를 모두 합친 `CompletableFuture` 를 생성한다.
* 단, `CompletableFuture<Void>` 형으로 값을 반환받을 수 없다.

### join
* 여러 CF를 동시 실행하고 모두 실행될 때까지 기다려서 그 결과를 합치고자 할때는 `Stream`, `join`을 사용한다.
```
String combined = Stream.of(future1, future2, future3)
  .map(CompletableFuture::join)
  .collect(Collectors.joining(" "));

assertEquals("Hello Beautiful World", combined);
```
* `CompletableFuture.join` 메소드는 `get`과 유사하지만, 해당 Future가 비정상 종료될 경우 Unchecked Exception 을 던진다(`get`은 Checked Exception).
Unchecked Exception 이라서 `Stream.map` 에서 사용가능하다.

## *Async
* `thenApplyAsync` 등 `*Async` 메소드들은 별도의 쓰레드 풀에서 해당 작업을 수행한다.
* 기본적으로 `ForkJoinPool`을 사용하는데, 이는 피하고, 명확하게 `Executor`를 지정해줘야한다.
쓰레드풀의 명확한 설정값을 모르는 상태로 기본 `ForkJoinPool`을 사용하다가 장애가 나는 경우가 있다. 스스로 컨트롤 가능한 쓰레드풀을 사용할 것.

## supplyAsync과 후속 thenApply, thenAccept 의 쓰레드
* [java - In which thread does CompletableFuture's completion handlers execute in? - Stack Overflow](https://stackoverflow.com/questions/46060438/in-which-thread-does-completablefutures-completion-handlers-execute-in)
* 작업의 `complete()` 시점 이전 혹은 이후에 `thenXXX` 호출 여부에 따라 `thenXXX` 의 실행 쓰레드가 달라질 수 있다.
* 따라서 쓰레드를 확신하지 말고, 어디서 실행되어도 상관없게 작성해야 한다.

