# CompletableFuture research
* package `kr.pe.kwonnam.research.java.completablefuture`
* [JustAsFutureTest](src/test/java/kr/pe/kwonnam/research/java/completablefuture/JustAsFutureTest)
* [CompletableFutureAsyncTest.java](src/test/java/kr/pe/kwonnam/research/java/completablefuture/CompletableFutureAsyncTest.java)
* [CompletableFutureJava9Test.java](src/test/java/kr/pe/kwonnam/research/java/completablefuture/CompletableFutureJava9Test.java)

* https://kwonnam.pe.kr/wiki/java/8/completable_future
* [Java 11 CompletableFuture](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/CompletableFuture.html)

## Baeldung Guide To CompletableFuture
* [Guide To CompletableFuture](https://www.baeldung.com/java-completablefuture)
* [Java 9 CompletableFuture API Improvements](https://www.baeldung.com/java-9-completablefuture)
* [Completable Future Improvements in Java9](https://blog.knoldus.com/completablefuture-improvements-in-java9/)

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

## CompletableFuture 의 상속
* java 8의 `CompletableFuture` 는 사실상 상속하기 매우 어렵다.
* java 9에서는 `newIncompleteFuture`와 `completeAsync` 를 사용하여 조금 쉬워졌다.
* 상속시에 무조건 `CompletableFuture<U> newIncompleteFuture()`도 override 해야한다.
* 최초 시작지점이 보통 static 메소드인 `CompletableFuture.supplyAsync` 등일 가능성이 높은데,
이런 static 메소드들 까지 모두 새로 만들지 않으면 사실상 상속의 의미가 없다.
* 따라서 `CompletableFuture.defaultExecutor()`도 거의 읽기만 가능할 뿐, 이것만 override하는 것은 큰 의미가 없다.
* [Java 9 이후의 CompletableFuture의 상속 - completeAsync 사용](https://stackoverflow.com/a/56356109/1051402)
* [Java 8 이하의 CompletableFuture의 상속](https://stackoverflow.com/a/26607433/1051402)
* [NeoCompletableFuture - supplyAsync까지 모두 Override 예제](src/main/java/kr/pe/kwonnam/research/java/completablefuture/NeoCompletableFuture.java)

## copy()
* `copy()' 는 원본 `CompletableFuture`의 상태를 기본적으로 그대로 복제한다.
* 하지만 아직 원본의 작업이 끝나지 않은 상태를 복제한 뒤에 변경을 복제본에 가하면(`cancel() 등`) 그 여파는 복제본에만 반영된다.
* `CompletableFuture`를 반환하되, 해당 객체의 상태를 클라이언트가 변경할 수 없게 하고자 할 때 사용하면 될 듯.

## thenCompose, whenComplete, handle 의 차이점
* 셋 다 새로운 `CompletableFuture`를 리턴한다.
* `thenComplete' 는 앞선 실행이 오류가 없을 경우에만 후속으로 실행된다. `Stream.flatMap`과 유사하다.
* `whenComplete`, `handle` 은 앞선 실행이 성공으로 끝나든 예외가 발생하든 실행되면 성공값과 예외를 모두 인자로 받는다.
* `whenComplete` 는 그 자체로 종결되는 것이 목표다.
* `handle` 은 다시 새로운 `CompletableFuture` 작업이 시작된다.

## orTimeout
* 시간내에 complete 되지 않을 경우 `TimeoutException`이 발생하고, 시간내에 완료되면 그대로 결과를 반환한다.

## completeOnTimeout
* 시간내에 complete 되지 않을 경우 `completeOnTimeout`에 지정된 값이 결과로 반환되고, 성공한 `CompletableFuture`로 간주된다.

## delayedExecutor
* static 메소드로, 기반 executor를 무조건 지정된 시간만큼 지연을 시켜서 submit 이 되게끔 해주는 감싸진 executor 를 반환한다.
