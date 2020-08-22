# Java 11 HttpClient
* [HttpClient kwonnam wiki](https://kwonnam.pe.kr/wiki/java/httpclient)
* [Java 11 HttpClient Examples - Mkyong.com](https://mkyong.com/java/java-11-httpclient-examples/)
* [Java HTTP Client - Examples and Recipes](https://openjdk.java.net/groups/net/httpclient/recipes.html)
* TODO [Java theory and practice: Explore the new Java SE 11 HTTP Client and WebSocket APIs](https://developer.ibm.com/technologies/java/tutorials/java-theory-and-practice-3/)

## API
* [HttpClient.java](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpClient.html) : immutable, reusable
* [HttpRequest.java](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpRequest.html) : immutable, reusable
* [HttpResponse.java](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpResponse.html)
* [WebSocket.java](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/WebSocket.html)

## HTTP/2
* 기본적으로 HTTP/2 를 지원한다.
* 비록 HTTP/2로 설정을 하더라도, 필요하면 자동으로 HTTP/1.1 로 자동 다운그레이드를 한다.

## CompletableFuture
* `HttpClient` 에 지정된 executor 를 사용해서 후속작업을 진행한다.
* 단, 직접 작업이 들 끝난 상태에서 새로운 후속작업(`supplyAsync`)가 호출됐을 때만 그렇다.
이미 HTTP 호출작업이 끝난 상태에서 등록된 후속 작업은 호출자 쓰레드에서 실행된다. 이것은 `CompletableFuture`의 특징이다.
* 이 API로 생성된 `CompletableFuture`의 `obtrudeValue`와 `obtrudeException` 메소드는 항상 `UnsupportedOperationException` 내게 되어있다.
* `cancel` 메소드를 호출해도, HTTP 호출을 중단하지는 않지만, 덜 끝난 상태의 `CompletableFuture`라면 예외 상태로 끝난다.

## Examples
* [HttpClientSynchronousTest.java](src/test/java/kr/pe/kwonnam/research/java/httpclient/HttpClientSynchronousTest.java)
* [HttpClientAsynchronousTest.java](src/test/java/kr/pe/kwonnam/research/java/httpclient/HttpClientAsynchronousTest.java)