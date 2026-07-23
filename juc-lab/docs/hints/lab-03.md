# JUC Lab 03 提示

## 提示 1

查询和复制快照使用 `readLock`，修改价格使用 `writeLock`。

## 提示 2

固定结构：

```java
lock.lock();
try {
    // 访问共享状态
} finally {
    lock.unlock();
}
```

## 提示 3

读取 Map 后，根据结果是否为 null 创建 `OptionalInt.empty()` 或 `OptionalInt.of(value)`。
