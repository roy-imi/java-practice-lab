# Stream Lab 03 提示

## 提示 1

`Order.getItemNames()` 返回 `List<String>`。如果直接 map，会得到 `Stream<List<String>>`。

## 提示 2

`flatMap` 的 Lambda 需要返回 Stream，因此可以对每个订单的商品列表调用 `stream()`。

## 提示 3

展开后元素已经是单个 String，再执行 `distinct()` 和 `collect()`。
