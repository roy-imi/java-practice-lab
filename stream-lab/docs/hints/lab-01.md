# Stream Lab 01 提示

## 提示 1

先写出完整链条的类型变化：

```text
Stream<Order> -> Stream<Order> -> Stream<String> -> List<String>
```

## 提示 2

`filter` 的 Lambda 返回 boolean；`map` 的 Lambda 返回订单 id。

## 提示 3

最后使用 `Collectors.toList()`。不要在 `forEach` 中手动向外部 List 添加元素。
