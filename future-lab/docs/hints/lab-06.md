# 第 6 课提示

## 提示 1：竞速不是比价

`anyOf` 在任意一项完成时结束，不等待其余项。它选的是响应最快的报价，不保证选到全局最低价。

## 提示 2：类型

`anyOf` 返回 `CompletableFuture<Object>`。在 `thenApply` 里做一次显式类型转换，恢复为 `CompletableFuture<ShopQuote>`。

## 提示 3：无返回值任务

`runAsync` 接收 `Runnable`，所以 lambda 中调用 `auditWriter.accept(...)` 即可。显式传入 executor，避免悄悄使用公共线程池。
