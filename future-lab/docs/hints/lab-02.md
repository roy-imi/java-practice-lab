# 第 2 课提示

## 提示 1：超时不是失败结果

`get(timeout, TimeUnit.MILLISECONDS)` 会抛 `TimeoutException`。这只代表调用方不愿再等，不代表后台任务自动停了。

## 提示 2：异常分层

- 超时：`cancel(true)` 后返回降级文案。
- 任务内部报错：捕获 `ExecutionException`，从 `getCause()` 取得真实原因。
- 等待线程被中断：先 `Thread.currentThread().interrupt()`，再把原异常抛出去。

## 提示 3：结构

先保存 `Future<String>`，然后使用 `try/catch` 包围带超时的 `get`。`cancel(true)` 放在超时分支里，这样不会误取消正常完成的任务。
