# Java Future Lab

这是一个面向 Java 8 的渐进式练习项目。你会为一个“电商商品页与下单系统”补齐异步代码，从会用 `Future`，逐步走到能组合 `CompletableFuture`。

项目不会在题目代码里直接给答案：

- `src/main/java/.../exercises`：你要修改的练习区，初始代码可以编译，但运行会提示尚未完成。
- `docs/hints`：每题三档提示。先只看提示 1，仍然卡住再继续。
- `src/main/java/.../answers`：独立答案区。完成后再对照，也可由自检程序验证。
- `LearningConsole`：运行某一课的练习。
- `ExerciseCheck`：只调用你的练习代码，按行为验收，不读取答案包。
- `SelfCheck`：检验标准答案和演示服务，确保项目本身可靠。

## 开始

```bash
cd /Users/roy/Documents/代码学习/java-practice-lab/future-lab
mvn package
java -cp target/classes com.roy.futurelab.LearningConsole 1
```

看到 `TODO` 提示后，打开对应练习，例如：

```text
src/main/java/com/roy/futurelab/exercises/Lab01FutureBasics.java
```

修改后重新执行：

```bash
mvn package
java -cp target/classes com.roy.futurelab.LearningConsole 1
java -cp target/classes com.roy.futurelab.ExerciseCheck 1
```

`LearningConsole` 用于观察输出和耗时；`ExerciseCheck` 用于确认结果、并发耗时、异常降级等行为是否正确。

运行全部标准答案自检：

```bash
java -cp target/classes com.roy.futurelab.SelfCheck
```

查看某课标准答案的演示效果（不会调用练习代码）：

```bash
java -cp target/classes com.roy.futurelab.AnswerConsole 1
```

## 推荐顺序

| 课次 | 主题 | 核心 API |
| --- | --- | --- |
| 1 | 同时查询价格和库存 | `submit`、`Future.get`、`isDone` |
| 2 | 慢服务的超时与取消 | `get(timeout)`、`cancel`、`ExecutionException` |
| 3 | 异步流水线 | `supplyAsync`、`thenApply`、`thenAccept` |
| 4 | 串联依赖、合并独立任务 | `thenCompose`、`thenCombine` |
| 5 | 批量聚合与失败降级 | `allOf`、`whenComplete`、`exceptionally` |
| 6 | 谁先完成用谁、无返回值任务 | `anyOf`、`runAsync` |

先读 [学习地图](docs/LEARNING_MAP.md)，每完成一课再写下：

1. 哪些任务可以并行？
2. 当前线程在哪一步被阻塞？
3. 异常最终由谁处理？
4. 使用的是哪个线程池，谁负责关闭？

## 有意保留的 Java 8 边界

Java 8 的 `CompletableFuture` 没有 `orTimeout` 和 `completeOnTimeout`。第 2 课使用 `Future.get(timeout)` 学习超时；如果以后升级到 Java 9+，可以再比较新 API。业务项目中也不要随意把阻塞 I/O 全塞进公共线程池，应根据负载使用自定义线程池。
