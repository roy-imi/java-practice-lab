# JUC Lab 07 提示

## 提示 1

线程池构造器依次需要核心数、最大数、存活时间、时间单位、队列、线程工厂和拒绝策略。

## 提示 2

线程工厂可以捕获一个 AtomicInteger：

```java
task -> new Thread(
        task,
        prefix + "-" + sequence.incrementAndGet())
```

## 提示 3

关闭时先 `shutdown()` 和 `awaitTermination()`；超时才 `shutdownNow()`。捕获中断后要恢复中断标记。
