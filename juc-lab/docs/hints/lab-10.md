# JUC Lab 10 提示

本课有两个练习类：

- `JucLab10SynchronizedInventory`
- `JucLab10LockInventory`

## 提示 1

两种实现都必须使用 `while (stock < quantity)`，因为线程被唤醒不代表条件一定仍然满足。

## 提示 2

synchronized 版本：

```text
synchronized(monitor)
    -> 库存不足时 monitor.wait(millis, nanos)
    -> 补货后 monitor.notifyAll()
```

`wait` 会释放 monitor，返回前会重新获得它。

## 提示 3

ReentrantLock 版本：

```text
lockInterruptibly
try:
    while 条件不满足:
        remaining = condition.awaitNanos(remaining)
    修改库存
finally:
    unlock
```

补货必须在持有 lock 时调用 `signalAll()`。
