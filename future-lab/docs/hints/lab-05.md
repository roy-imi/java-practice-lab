# 第 5 课提示

## 提示 1：先处理单项失败，再聚合

如果直接 `allOf` 一组可能失败的 Future，只要一个失败，聚合也会失败。先在每个子任务上用 `exceptionally` 变成可接受的降级值。

## 提示 2：观察与恢复不同

`whenComplete` 用来写日志，它不应吞掉失败；紧随其后的 `exceptionally` 才负责提供替代值。

## 提示 3：`allOf` 的数组

`CompletableFuture.allOf` 接收 `CompletableFuture<?>...`。可以把列表转为数组。全部完成后，再按原列表顺序逐个 `join()`，即可保持输入顺序。
