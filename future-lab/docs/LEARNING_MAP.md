# 学习地图

## 先建立一个准确的心智模型

`Future<T>` 像一张“稍后领取 `T` 的取货单”：

- `ExecutorService.submit(...)` 提交任务并立即得到取货单。
- `get()` 取结果；结果没好时，调用它的线程会等待。
- `get(timeout, unit)` 只愿意等一段时间。
- `cancel(true)` 发出中断请求，不保证任务一定能停止；任务本身要正确响应中断。
- `ExecutionException` 是任务内部异常的外包装，真正原因在 `getCause()`。

`CompletableFuture<T>` 不只是一张取货单，还是一个可拼装的处理流水线：

- `supplyAsync`：异步产生一个值；`runAsync`：异步做一件无返回值的事。
- `thenApply`：`T -> U`，同步地转换上一步结果。
- `thenAccept`：消费结果，返回阶段变为 `CompletableFuture<Void>`。
- `thenCompose`：`T -> CompletableFuture<U>`，适合“下一步依赖上一步结果”。
- `thenCombine`：合并两个互不依赖的异步结果。
- `allOf`：等所有任务完成，但它自身只返回 `Void`。
- `anyOf`：任一任务先完成就继续，返回类型是 `Object`。
- `exceptionally`：只在失败时给出替代值。
- `handle`：无论成功失败都会执行，并且可以转换结果。
- `whenComplete`：无论成功失败都观察一下，不改变原结果，适合日志和指标。

## 最容易混淆的两组概念

### `thenApply` 与 `thenCompose`

如果函数直接返回普通值，用 `thenApply`：

```text
商品价格 -> 打折后的价格
```

如果函数会启动另一个异步任务，用 `thenCompose` 把两层 Future 压平：

```text
用户等级 -> 异步查询该等级的折扣
```

目标通常是 `CompletableFuture<折扣>`，而不是
`CompletableFuture<CompletableFuture<折扣>>`。

### `thenCompose` 与 `thenCombine`

- 后一个任务需要前一个任务的结果：`thenCompose`。
- 两个任务彼此独立，只在最后汇合：`thenCombine`。

## `get`、`join` 与非阻塞链

- `get()` 抛受检异常，适合与老式 `Future` API 协作。
- `join()` 把失败包装为非受检的 `CompletionException`，在流式组合中更方便。
- 最理想的异步边界通常是把 `CompletableFuture` 返回给调用方，让更外层决定何时等待。

本项目的控制台在最外层调用 `join()`，只是为了让命令行程序等到结果并展示。练习方法本身尽量返回流水线，不要过早 `get()`。

## 线程池纪律

不传 `Executor` 的 `supplyAsync`/`runAsync` 默认使用 `ForkJoinPool.commonPool()`。它是进程共享资源，阻塞 I/O 多时容易相互影响。本项目显式传入带名字的固定线程池，便于观察和关闭。

记住：创建线程池的一方负责关闭它；收到外部传入线程池的方法通常不应擅自 `shutdown()`。

## 学习验收

完成每一课后，不看答案解释以下问题：

1. 如果先对第一个 `Future` 调用 `get()`，第二个任务还会不会运行？
2. `cancel(true)` 为什么只是“请求取消”？
3. 为什么在流水线中间调用 `join()` 会削弱异步组合的价值？
4. 何时选择 `thenCompose`，何时选择 `thenCombine`？
5. `allOf` 为什么还要从原 Future 列表中取结果？
6. `anyOf` 的速度优势是否等于“价格一定最低”？
