# Java JUC Concurrency Lab

这是一个面向 Java 8 的并发编程练习模块，以订单处理、接口限流和秒杀库存为场景。

JUC 是 `java.util.concurrent` 及其子包的常用简称。课程不仅介绍 API，还会解释并发问题为什么出现、工具解决了什么问题，以及它们在生产系统中的边界。

## 开始

先阅读 [JUC 教程索引](docs/guides/README.md)，然后运行第 1 课：

```bash
cd /Users/roy/Documents/代码学习/java-practice-lab/juc-lab
mvn package
java -cp target/classes com.roy.juclab.JucLearningConsole 1
```

完成某一课后验收：

```bash
mvn package
java -cp target/classes com.roy.juclab.JucExerciseCheck 1
```

验证标准答案：

```bash
java -cp target/classes com.roy.juclab.JucSelfCheck
```

## 学习顺序

| Lab | 主题 | 核心 API 或概念 |
| --- | --- | --- |
| 01 | 线程生命周期与协作式中断 | `Thread.start`、`join`、`interrupt` |
| 02 | 原子库存与高竞争计数 | `AtomicInteger`、CAS、`LongAdder` |
| 03 | 读多写少缓存 | `ReentrantReadWriteLock`、`Lock` |
| 04 | 并发请求统计 | `ConcurrentHashMap`、`computeIfAbsent` |
| 05 | 启动协调与并发限流 | `CountDownLatch`、`Semaphore` |
| 06 | 生产者消费者流水线 | `BlockingQueue`、背压、毒丸 |
| 07 | 有界线程池 | `ThreadPoolExecutor`、拒绝策略、优雅关闭 |
| 08 | 秒杀综合练习 | 幂等、CAS、防超卖、并发验收 |
| 09 | 请求上下文 | `ThreadLocal`、线程池传播、恢复与清理 |
| 10 | 互斥锁与条件等待 | `synchronized`、`ReentrantLock`、`Condition` |
| 11 | 可见性与安全发布 | `volatile`、不可变快照、`AtomicInteger` 对比 |

Lab 10 和 Lab 11 是后续追加的课程，为避免打乱已有练习而保留新编号。如果从头
系统学习，更推荐顺序：

```text
01 -> 02 -> 10 -> 11 -> 03 -> 04 -> 05 -> 06 -> 07 -> 08 -> 09
```

推荐先完成 Lambda、Stream 和 Future 模块，再进入本模块。
