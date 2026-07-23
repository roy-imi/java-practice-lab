# Future 与 CompletableFuture API 指南

这份文档对应 `future-lab` 的六课练习。建议先阅读对应章节，再回到 `exercises` 包写代码。

## Lab 对照表

| Lab | Exercise | 本文知识点 |
| --- | --- | --- |
| Lab 01 | [Lab01FutureBasics.java](../../src/main/java/com/roy/futurelab/exercises/Lab01FutureBasics.java) | submit、get、isDone、并行提交 |
| Lab 02 | [Lab02TimeoutAndCancel.java](../../src/main/java/com/roy/futurelab/exercises/Lab02TimeoutAndCancel.java) | 超时、cancel、ExecutionException、中断 |
| Lab 03 | [Lab03Pipeline.java](../../src/main/java/com/roy/futurelab/exercises/Lab03Pipeline.java) | supplyAsync、thenApply、thenAccept、join |
| Lab 04 | [Lab04ComposeAndCombine.java](../../src/main/java/com/roy/futurelab/exercises/Lab04ComposeAndCombine.java) | thenCompose、thenCombine |
| Lab 05 | [Lab05BatchAndRecovery.java](../../src/main/java/com/roy/futurelab/exercises/Lab05BatchAndRecovery.java) | allOf、whenComplete、exceptionally、handle |
| Lab 06 | [Lab06RaceAndRun.java](../../src/main/java/com/roy/futurelab/exercises/Lab06RaceAndRun.java) | anyOf、runAsync |

---

## Lab 01｜ExecutorService 与 Future

> 对应练习：Lab01FutureBasics

### 从同步调用到 Future

普通方法调用会在当前线程执行。方法没有返回前，当前线程不能继续处理后面的代码。

如果价格查询和库存查询互不依赖，串行执行的总耗时接近两次查询耗时之和；先把两项任务都提交给线程池，它们可以重叠执行，总耗时更接近较慢的一项。

### executor.submit(task)

`submit` 把任务交给线程池。

- `task`：一个 `Callable`，即无参数、有返回值、允许抛异常的任务。
- 返回值：`Future`，结果类型与 Callable 返回类型相同。
- 调用效果：通常只提交任务，不等待任务完成。

```java
Future<Integer> answerFuture =
        executor.submit(() -> 40 + 2);
```

Lambda `() -> 40 + 2` 对应 Callable 的无参数 `call()` 方法。

### future.get()

- 参数：没有参数。
- 返回值：任务最终计算出的结果。
- 阻塞：任务未完成时，调用 `get()` 的线程会等待。
- 异常：等待线程被中断时抛 `InterruptedException`；任务内部失败时抛 `ExecutionException`。

```java
int answer = answerFuture.get();
// answer 为 42
```

### future.isDone()

- 参数：没有参数。
- 返回值：`boolean`。
- 正常完成、异常结束或被取消时都会返回 `true`。
- `true` 只表示任务已经结束，不表示任务一定成功。

```java
boolean finished = answerFuture.isDone();
```

并发状态与观察时机有关。即使任务代码很短，`isDone()` 也可能是 `true` 或 `false`。

### Consumer.accept(value)

Consumer 接收一个值但不返回结果。练习中的 `trace` 是调用方传入的 Consumer。

```java
Consumer<String> trace =
        message -> System.out.println(message);

trace.accept("任务是否完成：" + answerFuture.isDone());
```

这种设计让业务方法只决定记录什么，不绑定输出位置。

### 关键原则

先提交全部互不依赖的任务，再调用 `get()`：

```java
Future<Integer> first = executor.submit(firstTask);
Future<Integer> second = executor.submit(secondTask);

int firstValue = first.get();
int secondValue = second.get();
```

如果提交第一项后立刻 `get()`，第一项完成后才提交第二项，实际效果仍然接近串行。

---

## Lab 02｜超时、取消与异常

> 对应练习：Lab02TimeoutAndCancel

### future.get(timeout, unit)

- `timeout`：最多等待的数量，类型为 `long`。
- `unit`：时间单位，例如 `TimeUnit.MILLISECONDS`。
- 返回值：任务在期限内完成时的结果。
- `TimeoutException`：等待期限已到。
- `ExecutionException`：任务内部失败。
- `InterruptedException`：当前等待线程被中断。

```java
String value =
        future.get(200, TimeUnit.MILLISECONDS);
```

超时只代表调用方不再等待，后台任务不会自动停止。

### future.cancel(mayInterruptIfRunning)

- `mayInterruptIfRunning`：任务已经运行时，是否允许发送中断请求。
- `true`：允许请求中断。
- `false`：不打断已经运行的任务。
- 返回值：取消是否成功改变了任务状态。

```java
boolean cancelled = future.cancel(true);
```

取消是协作式的。任务代码若忽略中断，仍可能继续运行。

### ExecutionException.getCause()

`ExecutionException` 是 `Future.get()` 为任务异常添加的外包装，真实原因在 `getCause()` 中。

```java
try {
    String value = future.get();
} catch (ExecutionException failed) {
    Throwable realCause = failed.getCause();
    System.out.println(realCause.getMessage());
}
```

### Thread.currentThread().interrupt()

抛出 `InterruptedException` 时，JVM 会清除线程的中断标志。如果当前层不能完整处理中断，应恢复标志并继续上抛。

```java
catch (InterruptedException interrupted) {
    Thread.currentThread().interrupt();
    throw interrupted;
}
```

### 完整等待结构示例

```java
Future<String> future =
        executor.submit(() -> slowService.load());

try {
    return future.get(200, TimeUnit.MILLISECONDS);
} catch (TimeoutException timeout) {
    future.cancel(true);
    return "默认结果";
} catch (ExecutionException failed) {
    return "服务失败：" + failed.getCause().getMessage();
} catch (InterruptedException interrupted) {
    Thread.currentThread().interrupt();
    throw interrupted;
}
```

这里的 `slowService` 只是某个慢服务的占位名称。

---

## Lab 03｜CompletableFuture 流水线

> 对应练习：Lab03Pipeline

普通 Future 只能表示“以后会有结果”，不擅长声明结果产生后的连续处理。CompletableFuture 把流程拆成多个阶段。

### CompletableFuture.supplyAsync(supplier, executor)

- `supplier`：无参数、有返回值的 `Supplier`。
- `executor`：负责执行任务的线程池。
- 返回值：代表 supplier 最终结果的 CompletableFuture。

```java
CompletableFuture<Integer> numberFuture =
        CompletableFuture.supplyAsync(
                () -> 10,
                executor);
```

### previousFuture.thenApply(transformer)

- `transformer`：接收上一步结果并返回新结果的 `Function`。
- 返回值：保存转换结果的新 CompletableFuture。
- 上一步失败时不会执行 transformer，异常会继续向后传播。

```java
CompletableFuture<Integer> doubledFuture =
        numberFuture.thenApply(number -> number * 2);
```

### previousFuture.thenAccept(action)

- `action`：接收上一步结果但不产生新业务值的 `Consumer`。
- 返回值：结果类型为 `Void` 的 CompletableFuture。

```java
CompletableFuture<Void> printFuture =
        doubledFuture.thenAccept(
                value -> System.out.println(value));
```

### future.join()

- 参数：没有参数。
- 返回值：最终结果。
- 未完成时会阻塞当前线程。
- 失败时抛非受检的 `CompletionException`，真实原因在 `getCause()`。

```java
printFuture.join();
```

### 完整流水线示例

```java
CompletableFuture<Void> pipeline =
        CompletableFuture
                .supplyAsync(() -> 10, executor)
                .thenApply(number -> number * 2)
                .thenAccept(
                        value -> System.out.println(value));

pipeline.join();
// 输出 20
```

练习方法应尽量返回整条流水线，让系统最外层决定何时等待。

### 带 Async 和不带 Async 的区别

不带 `Async` 后缀的 `thenApply`、`thenAccept` 通常由完成上一阶段的线程继续执行。

带 `Async` 的版本会重新调度任务；可以显式传入 Executor：

```java
future.thenApplyAsync(
        value -> transform(value),
        executor);
```

不要因为方法名带 Async 就无条件使用，先判断是否真的需要切换执行线程。

---

## Lab 04｜thenCompose 与 thenCombine

> 对应练习：Lab04ComposeAndCombine

### thenCompose：前后依赖

调用形式：

```java
previousFuture.thenCompose(nextAsyncTask)
```

- `nextAsyncTask` 接收上一步结果。
- 它必须返回另一个异步阶段。
- thenCompose 把两层 Future 压成一层。
- 适合“第二个异步任务依赖第一个结果”。

```java
CompletableFuture<String> userNameFuture =
        CompletableFuture.completedFuture("小明");

CompletableFuture<String> emailFuture =
        userNameFuture.thenCompose(
                userName -> queryEmailAsync(userName));
```

如果改用 `thenApply`，结果会变成两层 CompletableFuture。

### thenCombine：独立任务汇合

调用形式：

```java
firstFuture.thenCombine(secondFuture, combiner)
```

- `secondFuture`：另一项可独立运行的异步任务。
- `combiner`：接收两个结果并返回合并结果的 `BiFunction`。
- 两个 Future 都成功完成后才执行 combiner。
- 不要求哪一个先完成。

```java
CompletableFuture<Integer> priceFuture =
        CompletableFuture.completedFuture(100);
CompletableFuture<Integer> deliveryFuture =
        CompletableFuture.completedFuture(10);

CompletableFuture<Integer> totalFuture =
        priceFuture.thenCombine(
                deliveryFuture,
                (price, delivery) -> price + delivery);

int total = totalFuture.join();
// total 为 110
```

### 选择方法

```text
后一个任务需要前一个结果 -> thenCompose
两个任务可以独立执行     -> thenCombine
普通值转换               -> thenApply
```

不要为了取得中间值而在流水线中调用 `get()` 或 `join()`，应继续组合 Future。

---

## Lab 05｜批量聚合与异常恢复

> 对应练习：Lab05BatchAndRecovery

### whenComplete(observer)

- `observer`：接收 `result` 和 `error` 的 `BiConsumer`。
- 成功时 `error` 为 `null`。
- 失败时 `error` 保存异常。
- 适合日志和指标，通常不改变原结果或异常。

```java
CompletableFuture<String> observed =
        future.whenComplete((result, error) -> {
            if (error == null) {
                System.out.println("成功：" + result);
            } else {
                System.out.println("失败：" + error.getMessage());
            }
        });
```

### exceptionally(recovery)

- `recovery`：接收异常，返回与正常结果相同类型的备用值。
- 原任务成功时保留原值。
- 原任务失败时使用 recovery 返回值。

```java
CompletableFuture<String> safeFuture =
        future.exceptionally(
                error -> "默认结果");
```

### handle(handler)

- 成功或失败都会执行。
- 同时接收正常结果和异常。
- 可以返回一个新结果。

```java
CompletableFuture<String> message =
        future.handle((result, error) ->
                error == null ? result : "失败");
```

### CompletableFuture.allOf(futures)

- 参数：数量不固定的一组 CompletableFuture。
- 返回值：结果类型为 `Void`，只表示全部完成。
- 所有输入成功时才成功。
- 任意输入失败时，allOf 也会失败。
- 空数组会得到一个已经完成的 Future。

```java
CompletableFuture<String> first =
        CompletableFuture.completedFuture("A");
CompletableFuture<String> second =
        CompletableFuture.completedFuture("B");

CompletableFuture<Void> all =
        CompletableFuture.allOf(first, second);

List<String> values = all.thenApply(ignored ->
        Arrays.asList(first.join(), second.join())
).join();
// values 为 ["A", "B"]
```

allOf 不返回各项结果，因此必须保留原 Future 集合。allOf 完成后再对原 Future 调用 `join()`，只是读取已完成结果。

### Fan-out 与 Fan-in

```text
输入列表
  -> 每项创建异步任务（fan-out）
  -> 每项独立恢复异常
  -> allOf 等待全部任务（fan-in）
  -> 按原 Future 顺序收集结果
```

按原 Future 列表收集，可以保持输入顺序；回调触发顺序只代表完成速度。

---

## Lab 06｜anyOf 与 runAsync

> 对应练习：Lab06RaceAndRun

### CompletableFuture.anyOf(futures)

- 参数：一组参与竞速的 CompletableFuture。
- 返回值：结果类型为 `Object`。
- 最先结束的任务决定 anyOf 的结果。
- 最先结束的任务如果失败，anyOf 会立即失败。
- 它等待的是“第一个完成”，不是“第一个成功”。
- 空数组会返回一个无法自动完成的 Future。

```java
CompletableFuture<String> first =
        CompletableFuture.supplyAsync(
                () -> serviceA.load(), executor);
CompletableFuture<String> second =
        CompletableFuture.supplyAsync(
                () -> serviceB.load(), executor);

CompletableFuture<String> winner =
        CompletableFuture
                .anyOf(first, second)
                .thenApply(value -> (String) value);

String fastestValue = winner.join();
```

只有在所有输入结果类型一致时，这种强制类型转换才安全。

anyOf 完成后，其他任务不会自动取消。真实项目若需要取消落后任务，必须保留原 Future 并设计取消策略。

### CompletableFuture.runAsync(action, executor)

- `action`：无参数、无返回值的 `Runnable`。
- `executor`：执行 action 的线程池。
- 返回值：结果类型为 `Void` 的 CompletableFuture。

```java
CompletableFuture<Void> logFuture =
        CompletableFuture.runAsync(
                () -> System.out.println("记录日志"),
                executor);

logFuture.join();
```

`supplyAsync` 用于产生结果，`runAsync` 用于只执行动作。

---

## 通用知识｜线程池所有权

> 对应 Lab：Lab 01 至 Lab 06

不传 Executor 时，CompletableFuture 异步工厂方法默认使用公共 ForkJoinPool。

阻塞 I/O 可能长期占用公共线程，业务代码通常应显式使用合适的线程池。

谁创建线程池，通常由谁关闭。收到外部传入 Executor 的方法不应擅自调用 `shutdown()`。
