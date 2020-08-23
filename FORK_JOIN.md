# ForkJoin Framework
* [Guide to the Fork/Join Framework in Java](https://www.baeldung.com/java-fork-join)

## Fork / Join?
* Fork(갈라지다, 나뉘다) : 먼저 작업을 비동기적으로 실행할 수 있는 여러개의 충분히 작은 작업으로 재귀적으로 나눈다.
* Join(연결하다, 합쳐지다) : 분할된 작업들의 결과가 재귀적으로 하나의 결과로 합쳐진다. 결과 반환이 불필요한 경우(void)에는
모든 작업이 끝날 때 까지 기다린다.

## ForkJoinPool
* [ExecutorService](https://www.baeldung.com/java-executor-service-tutorial)의 구현체
* 작업 쓰레드를 관리하고 쓰레드 풀의 상태와 성능에 대한 정보 제공
