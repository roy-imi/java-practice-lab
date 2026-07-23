# JUC Lab 02 提示

## 提示 1

先读取 current。若 `current < quantity`，直接返回 false。

## 提示 2

使用 `compareAndSet(current, current - quantity)` 把检查与扣减联系起来。

## 提示 3

CAS 返回 false 说明状态已变化，使用循环重新读取。只有成功更新后才调用 `successfulOrders.increment()`。
