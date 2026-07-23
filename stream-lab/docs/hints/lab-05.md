# Stream Lab 05 提示

## 提示 1

`groupingBy` 的第一个参数决定 Map 的 key，本题 key 是城市。

## 提示 2

第二个参数是下游 Collector。使用 `summingInt` 把每组订单金额直接汇总为 Integer。

## 提示 3

`partitioningBy` 的 Predicate 返回 true 时，订单进入 `true` 对应的 List。
