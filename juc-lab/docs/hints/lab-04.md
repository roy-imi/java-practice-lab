# JUC Lab 04 提示

## 提示 1

使用 `computeIfAbsent(endpoint, ...)` 一步完成“没有就创建”。

## 提示 2

mappingFunction 返回新的 `LongAdder`，得到计数器后调用 `increment()`。

## 提示 3

snapshot 新建 HashMap，然后遍历 ConcurrentHashMap，把每个 `LongAdder.sum()` 放入结果。
