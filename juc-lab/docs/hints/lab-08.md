# JUC Lab 08 提示

## 提示 1

幂等入口可以使用：

```java
results.computeIfAbsent(
        requestId,
        this::reserveOne)
```

## 提示 2

reserveOne 先读库存。等于 0 返回 soldOut，大于 0 时尝试 CAS 扣一。

## 提示 3

CAS 失败只表示其他线程抢先修改了库存，应继续循环，而不是立即返回售罄。
