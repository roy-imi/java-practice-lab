# Stream Lab 02 提示

## 提示 1

排序时 Stream 中必须仍然是 `Order`，因为客户名称没有订单金额。

## 提示 2

`Comparator.comparingInt(Order::getAmountCents)` 默认从小到大，观察哪个方法可以反转。

## 提示 3

推荐顺序：

```text
filter -> sorted -> map -> distinct -> limit -> collect
```
