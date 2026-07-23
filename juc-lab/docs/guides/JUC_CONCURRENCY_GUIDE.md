# Java JUC 并发与高并发实战指南

JUC 通常指 `java.util.concurrent`、`java.util.concurrent.atomic` 和
`java.util.concurrent.locks`。它提供线程池、并发容器、同步器、原子类和显式锁。

高并发不是“多创建线程”。真正目标是在明确的资源上限内，同时获得：

- 正确性：不重复、不丢失、不超卖。
- 吞吐量：单位时间完成足够多的请求。
- 延迟：尤其关注 P95、P99 等尾延迟。
- 稳定性：过载时能够限流、拒绝或降级，而不是拖垮整个进程。
- 可恢复性：任务失败、中断、超时后资源能够释放。
- 可观测性：能够看到活跃线程、队列长度、拒绝数和任务耗时。

## Lab 对照表

| Lab | Exercise | 本文知识点 |
| --- | --- | --- |
| Lab 01 | `JucLab01ThreadLifecycle` | start、join、interrupt、线程状态、happens-before |
| Lab 02 | `JucLab02AtomicInventory` | 竞态条件、AtomicInteger、CAS、LongAdder、volatile |
| Lab 03 | `JucLab03PriceCache` | Lock、ReentrantLock、ReadWriteLock、死锁 |
| Lab 04 | `JucLab04RequestStatistics` | ConcurrentHashMap、computeIfAbsent、弱一致性 |
| Lab 05 | `JucLab05ConcurrencyGate` | CountDownLatch、CyclicBarrier、Semaphore |
| Lab 06 | `JucLab06BlockingQueuePipeline` | BlockingQueue、生产者消费者、背压、毒丸 |
| Lab 07 | `JucLab07ThreadPoolFactory` | ThreadPoolExecutor 七个参数、拒绝策略、关闭 |
| Lab 08 | `JucLab08FlashSaleService` | 防超卖、幂等、组合并发工具、分布式边界 |

---

## 预备知识｜并发、并行与高并发指标

### 并发与并行

并发描述多个任务在一段时间内交替推进；并行描述多个任务在同一时刻真正运行。

单核 CPU 也可以并发执行多个线程，但同一瞬间通常只有一个线程在执行 Java 指令。多核 CPU 才能让多个计算线程真正并行。

### QPS、延迟和在途请求

粗略估算：

```text
平均在途请求数 ≈ 每秒请求数 × 平均响应秒数
```

例如 2,000 QPS、平均响应 0.05 秒，大约有 100 个请求同时在途。这个估算能帮助理解为什么下游变慢时，线程和队列会迅速堆积。

不能只看平均延迟。少量极慢请求会占用线程、连接和内存，因此生产环境通常还看：

- P50：一半请求不超过该延迟。
- P95：95% 的请求不超过该延迟。
- P99：99% 的请求不超过该延迟。
- 超时率、错误率、拒绝率。
- 活跃线程数、线程池队列长度。

### 并发问题的三个核心维度

原子性：一个操作是否会被拆开并与其他线程交错。

```java
count++;
```

它通常包含读取、加一、写回，不是一个不可分割的动作。

可见性：一个线程写入的值，另一个线程何时能够看见。

有序性：编译器、JIT 和 CPU 在不改变单线程语义的前提下可能重排指令，多线程观察到的顺序可能不同。

### happens-before

happens-before 是 Java 内存模型用来描述跨线程可见性和顺序的规则。常见关系：

- 解锁一个锁，happens-before 后续对同一锁的加锁。
- 对 volatile 字段的写，happens-before 后续对该字段的读。
- 调用 `Thread.start()` 前的操作，对新线程可见。
- 线程中的操作，在另一个线程从 `join()` 成功返回后可见。
- `CountDownLatch.countDown()` 前的操作，对成功从 `await()` 返回的线程可见。

这些关系比“加一点 sleep 应该就能看到了”可靠得多。

---

## Lab 01｜线程生命周期与协作式中断

> 对应练习：JucLab01ThreadLifecycle

### Thread 构造器

```java
Thread thread = new Thread(task, "order-worker-1");
```

- `task`：线程启动后执行的 `Runnable`。
- `name`：线程名称。生产环境应命名，线程转储和日志才能定位任务来源。
- 构造线程不会开始执行任务。

### start() 与 run()

```java
thread.start();
```

`start()` 请求 JVM 启动新线程，新线程随后执行 `run()`。

```java
thread.run();
```

直接调用 `run()` 只是当前线程的一次普通方法调用，没有并发效果。

同一个 Thread 对象只能成功 `start()` 一次。再次启动会抛出
`IllegalThreadStateException`。

### join()

```java
thread.join();
```

- 参数为空：一直等到目标线程结束。
- `join(long millis)`：最多等待指定毫秒。
- `join(long millis, int nanos)`：增加纳秒部分。
- 等待期间当前线程可能被中断，因此方法抛出 `InterruptedException`。

典型用途是主线程等待多个工作线程完成：

```java
for (Thread worker : workers) {
    worker.start();
}
for (Thread worker : workers) {
    worker.join();
}
```

先全部 `start`，再逐个 `join`。如果启动一个就立即等待一个，任务会退化成串行执行。

### interrupt()

```java
worker.interrupt();
```

`interrupt()` 不是强制杀死线程，而是发送协作式中断请求：

- 线程处于 `sleep`、`wait`、`join` 或可中断的阻塞方法时，通常抛出
  `InterruptedException`。
- 线程正在普通计算时，中断标记被设置，代码需要主动检查。
- 抛出 `InterruptedException` 时，中断标记通常会被清除。

循环任务应主动响应：

```java
while (!Thread.currentThread().isInterrupted()) {
    processNextBatch();
}
```

捕获中断后如果当前方法不能继续向上抛，通常要恢复标记：

```java
try {
    queue.put(task);
} catch (InterruptedException interrupted) {
    Thread.currentThread().interrupt();
    return;
}
```

不推荐吞掉中断：

```java
catch (InterruptedException ignored) {
    // 上层已经无法知道有人请求停止
}
```

也不要使用 `Thread.stop()`。它可能在任意指令处终止线程，使共享对象停留在不一致状态。

### 常见线程状态

- NEW：已经创建，还没有 start。
- RUNNABLE：可运行，可能正在执行，也可能等待 CPU。
- BLOCKED：等待进入 synchronized 临界区。
- WAITING：无限期等待，例如无超时的 `join`、`wait`、`park`。
- TIMED_WAITING：有限期等待，例如 `sleep`、有超时的 `await`。
- TERMINATED：执行结束。

注意：Java 的 RUNNABLE 同时覆盖操作系统层面的“正在运行”和“可运行等待调度”。

### 生产建议

Lab 01 直接创建 Thread 是为了理解基础。业务系统通常把任务提交给受控线程池，避免每个请求都创建新线程。

---

## Lab 02｜原子库存、CAS 与高竞争计数

> 对应练习：JucLab02AtomicInventory

### 竞态条件

下面代码即使字段是共享的，也会丢失更新：

```java
stock--;
```

两个线程可能同时读到 stock 为 1，都计算为 0，然后都宣称购买成功。这就是“先检查库存，再扣库存”之间被其他线程插入造成的竞态。

### AtomicInteger 常用 API

```java
AtomicInteger stock = new AtomicInteger(100);
```

构造参数 `initialValue` 是初始整数。

读取和直接写入：

```java
int current = stock.get();
stock.set(80);
```

递增、递减：

```java
int afterIncrement = stock.incrementAndGet();
int beforeIncrement = stock.getAndIncrement();
int afterDecrement = stock.decrementAndGet();
```

`incrementAndGet()` 返回增加后的值，`getAndIncrement()` 返回增加前的值。

加指定数值：

```java
int after = stock.addAndGet(-3);
int before = stock.getAndAdd(-3);
```

### compareAndSet(expectedValue, newValue)

- `expectedValue`：你认为当前应该是什么值。
- `newValue`：当前值确实等于期望值时，要写入的新值。
- 返回 true：比较和更新成功。
- 返回 false：其他线程已经改过值，本次没有更新。

库存扣减需要把“检查”和“写入”变成一个原子条件更新：

```java
while (true) {
    int current = stock.get();
    if (current < quantity) {
        return false;
    }

    boolean updated =
            stock.compareAndSet(
                    current,
                    current - quantity);
    if (updated) {
        return true;
    }
}
```

CAS 失败不代表业务失败，只说明读取后有竞争，需要重新读取状态并判断。

### CAS 的优点和成本

优点：

- 没有互斥锁挂起和唤醒的固定成本。
- 临界区很小、冲突不高时通常表现良好。

成本：

- 高竞争下会反复自旋，占用 CPU。
- 适合一个或少量状态的原子变化，不适合很长的业务临界区。
- 引用 CAS 可能遇到 ABA 问题，可根据场景使用带版本的
  `AtomicStampedReference`。

### updateAndGet

简单更新也可以写成：

```java
int remaining = stock.updateAndGet(
        current -> Math.max(0, current - 1));
```

更新函数可能因为 CAS 重试被调用多次，因此必须没有外部副作用。库存是否成功还需要明确区分，手写 CAS 循环往往更清晰。

### LongAdder

```java
LongAdder requests = new LongAdder();
requests.increment();
long value = requests.sum();
```

`LongAdder` 在高竞争下把热点分散到多个内部计数单元，读取时再求和，吞吐通常优于所有线程竞争同一个 `AtomicLong`。

适合：

- 请求数、命中数等统计指标。
- 允许读取到某一时刻的近似快照。

不适合：

- 库存余额、账户余额等必须基于当前精确值做条件更新的状态。
- 需要 `compareAndSet` 的业务。

### volatile、AtomicInteger 和锁的区别

`volatile` 保证单次读写的可见性和相关顺序，但不把复合操作变成原子操作：

```java
private volatile int count;

count++; // 仍然不安全
```

选择思路：

- 一个标记的可见性：volatile。
- 一个数值的简单原子更新：AtomicInteger 或 AtomicLong。
- 高竞争统计：LongAdder。
- 多个变量需要维持联合不变式：锁。

---

## Lab 03｜显式锁与读多写少缓存

> 对应练习：JucLab03PriceCache

### Lock 的基本结构

```java
Lock lock = new ReentrantLock();

lock.lock();
try {
    updateSharedState();
} finally {
    lock.unlock();
}
```

`unlock()` 必须在 finally 中。临界区抛异常时如果没有释放锁，其他线程可能永久等待。

### ReentrantLock 构造参数

```java
new ReentrantLock();
new ReentrantLock(true);
```

- 无参或 false：非公平锁，允许刚到达的线程插队，通常吞吐更高。
- true：公平锁，等待时间最长的线程更有机会先获得锁，但调度成本更高。

“公平”不等于业务请求严格按顺序完成。

### tryLock

```java
if (lock.tryLock()) {
    try {
        update();
    } finally {
        lock.unlock();
    }
} else {
    fallback();
}
```

带超时版本：

```java
if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {
    try {
        update();
    } finally {
        lock.unlock();
    }
}
```

它让系统可以在竞争严重时超时、降级或返回繁忙，而不是无限等待。

### ReentrantReadWriteLock

```java
ReentrantReadWriteLock rw =
        new ReentrantReadWriteLock();
Lock readLock = rw.readLock();
Lock writeLock = rw.writeLock();
```

规则：

- 多个读线程可以同时持有读锁。
- 写锁与其他读锁、写锁互斥。
- 适合读取明显多于写入，并且读操作不是极短的场景。

读缓存：

```java
readLock.lock();
try {
    return cache.get(key);
} finally {
    readLock.unlock();
}
```

写缓存：

```java
writeLock.lock();
try {
    cache.put(key, value);
} finally {
    writeLock.unlock();
}
```

读写锁不一定更快。数据量小、临界区短或写入频繁时，锁管理成本可能抵消并发读收益，应通过基准测试判断。

### 锁升级和降级

持有读锁时直接申请写锁容易发生等待，不应依赖“读锁升级”。

写锁降级为读锁可以按以下顺序进行：

```text
持有写锁
  -> 获取读锁
  -> 释放写锁
  -> 在读锁下读取
```

### 死锁的四个必要条件

1. 资源互斥。
2. 持有一个资源并等待另一个资源。
3. 资源不能被强制抢占。
4. 存在循环等待。

常用预防办法：

- 所有代码按统一顺序获得多把锁。
- 缩小锁范围，不在锁内执行网络请求。
- 使用 `tryLock` 和超时。
- 减少嵌套锁。
- 通过线程转储检查互相等待的锁。

---

## Lab 04｜ConcurrentHashMap 与并发统计

> 对应练习：JucLab04RequestStatistics

### 线程安全方法不等于组合操作安全

错误的“先检查再放入”：

```java
if (!map.containsKey(endpoint)) {
    map.put(endpoint, new LongAdder());
}
map.get(endpoint).increment();
```

每一个方法单独线程安全，但两个线程都可能在检查后创建新值，导致某个计数器及其增量被覆盖。

### computeIfAbsent(key, mappingFunction)

- `key`：要查找的键。
- `mappingFunction`：键不存在时，根据 key 创建 value。
- 返回值：已经存在或刚创建的 value。
- mappingFunction 返回 null 时，不建立映射。

```java
map.computeIfAbsent(
        endpoint,
        key -> new LongAdder())
   .increment();
```

对同一个 key 的初始化由 ConcurrentHashMap 协调。

mappingFunction 应该短小、无副作用，不能递归修改同一个 key。长时间阻塞会拖慢竞争同一区域的其他操作。

### merge

普通整数计数也可以写：

```java
counts.merge(endpoint, 1L, Long::sum);
```

- 第一个参数：key。
- 第二个参数：key 不存在时使用的初始 value。
- 第三个参数：key 已存在时，合并旧值和新值的函数。

高竞争统计使用 `ConcurrentHashMap<String, LongAdder>` 通常能进一步减少同一个 Long 值上的竞争。

### putIfAbsent

```java
Value existing = map.putIfAbsent(key, newValue);
```

- key 不存在时写入并返回 null。
- key 已存在时不覆盖，返回旧值。

适合已经在外部构造好候选值的场景。若构造代价较高，`computeIfAbsent` 可以避免不必要创建。

### 弱一致遍历

ConcurrentHashMap 的迭代器不会像普通 HashMap 那样在并发修改时快速失败。遍历通常能反映遍历期间的一部分更新，但不保证是某一个瞬间的全局快照。

统计监控通常可以接受弱一致数据。账户结算等强一致业务不能把这种遍历当作事务快照。

### 其他常用并发容器

`CopyOnWriteArrayList`：

- 写入时复制整个底层数组。
- 读操作无锁、迭代稳定。
- 适合读极多、写极少的小型监听器列表。
- 不适合频繁写入或大列表。

`ConcurrentLinkedQueue`：

- 无界、非阻塞 FIFO 队列。
- `offer` 和 `poll` 不会因为容量阻塞。
- 不能提供有界队列的背压。

---

## Lab 05｜CountDownLatch、CyclicBarrier 与 Semaphore

> 对应练习：JucLab05ConcurrencyGate

### CountDownLatch

```java
CountDownLatch ready =
        new CountDownLatch(serviceCount);
```

构造参数 `count` 表示还要发生多少次完成信号才能放行等待者。

工作线程完成准备：

```java
try {
    initialize();
} finally {
    ready.countDown();
}
```

等待所有服务准备：

```java
ready.await();
```

带超时：

```java
boolean completed =
        ready.await(2, TimeUnit.SECONDS);
```

- 返回 true：计数在超时前归零。
- 返回 false：等待超时。
- 等待可能抛出 InterruptedException。

`countDown()` 不会阻塞，计数到 0 后继续调用也不会变成负数。

CountDownLatch 是一次性的，归零后不能重置。

### 两类常见用法

等待 N 个任务完成：

```text
主线程 await
每个工作线程完成后 countDown
```

同时放行 N 个任务：

```text
所有工作线程先 await
主线程准备好后调用一次 countDown
```

后者常用于并发测试，让多个线程尽量同时开始，扩大竞争窗口。

### CyclicBarrier

```java
CyclicBarrier barrier =
        new CyclicBarrier(
                participantCount,
                () -> mergeResults());
```

- `parties`：每一轮必须到达屏障的参与者数量。
- `barrierAction`：所有参与者到齐后，在放行前执行的可选动作。
- 每个参与者调用 `await()`。
- 一轮结束后可以重复使用。

区别：

- CountDownLatch：一个或多个线程等待事件计数归零，一次性。
- CyclicBarrier：一组线程互相等待全部到齐，可以循环多轮。

### Semaphore

```java
Semaphore permits = new Semaphore(10);
```

构造参数 `permits` 是同时可用的许可数。

公平模式：

```java
new Semaphore(10, true);
```

公平模式更接近等待顺序发放许可，但通常吞吐较低。

基本用法：

```java
boolean acquired = false;
try {
    permits.acquire();
    acquired = true;
    callDownstream();
} finally {
    if (acquired) {
        permits.release();
    }
}
```

只有成功获得许可后才能 release，否则会凭空增加许可数。

非阻塞尝试：

```java
if (!permits.tryAcquire()) {
    rejectAsBusy();
}
```

带超时：

```java
boolean acquired =
        permits.tryAcquire(
                50,
                TimeUnit.MILLISECONDS);
```

### 并发限制不等于速率限制

Semaphore 限制“同一时刻最多多少任务”，不是“每秒最多多少请求”。

例如每个任务只执行 1 毫秒，10 个许可每秒仍可能完成很多批。QPS 速率限制通常使用令牌桶或漏桶算法。

Semaphore 适合保护：

- 数据库连接或第三方接口。
- 内存密集型任务。
- 不希望被无限并发调用的旧系统。

---

## Lab 06｜BlockingQueue、生产者消费者与背压

> 对应练习：JucLab06BlockingQueuePipeline

### 为什么使用阻塞队列

生产者和消费者速度往往不同。BlockingQueue 同时解决：

- 线程安全的任务传递。
- 队列为空时消费者等待。
- 有界队列已满时生产者等待或失败。
- 通过中断取消等待。

### 四组常用方法

| 行为 | 抛异常 | 返回特殊值 | 一直阻塞 | 超时等待 |
| --- | --- | --- | --- | --- |
| 插入 | `add(e)` | `offer(e)` | `put(e)` | `offer(e, time, unit)` |
| 取出 | `remove()` | `poll()` | `take()` | `poll(time, unit)` |
| 查看队首 | `element()` | `peek()` | 不适用 | 不适用 |

特殊值通常是插入失败返回 false、取不到返回 null。

### ArrayBlockingQueue(capacity)

```java
BlockingQueue<Task> queue =
        new ArrayBlockingQueue<>(100);
```

- `capacity`：固定容量，必须大于 0。
- 内部使用数组，创建后容量不变。
- 可以选择公平模式：

```java
new ArrayBlockingQueue<>(100, true);
```

有界队列把内存上限和过载行为显式化，适合生产线程池和生产者消费者系统。

### LinkedBlockingQueue

```java
new LinkedBlockingQueue<>(100);
```

传入容量时是有界队列。不传容量时上限接近
`Integer.MAX_VALUE`，消费跟不上时可能堆积大量对象。

### SynchronousQueue

没有实际存储容量。每次 put 必须等另一个线程 take，适合直接移交任务，但需要足够的消费者或明确拒绝策略。

### 背压

背压表示下游处理不过来时，系统把压力反馈给上游。

有界队列满后可以选择：

- `put`：生产者等待，自然减慢上游。
- `offer`：立即失败，调用方降级或拒绝。
- 超时 `offer`：等待有限时间后失败。
- 把任务持久化到消息队列，由独立消费者处理。

无界堆积只是把过载转换成内存占用和更高延迟，并没有消除压力。

### 毒丸

毒丸是一个特殊队列元素，用于通知消费者正常退出：

```java
while (true) {
    Task task = queue.take();
    if (task == POISON) {
        break;
    }
    process(task);
}
```

如果有 N 个消费者，通常要投递 N 个毒丸，否则只有部分消费者能退出。

生产者必须先投递全部普通任务，再投递毒丸。

### 保持结果顺序

多个消费者完成顺序不可预测。如果业务要求输出与输入顺序一致，可以给任务携带原下标：

```text
(0, order-A)
(1, order-B)
(2, order-C)
```

消费者把结果写回对应位置。等待所有消费者结束后，再按下标组装结果。

### 失败处理

教学练习假设 processor 正常返回。生产实现还要定义：

- 单个任务抛异常后是否重试。
- 重试次数和退避时间。
- 最终失败是否进入死信队列。
- 消费者异常退出后由谁补充。
- 进程关闭时未处理任务如何保存。

---

## Lab 07｜ThreadPoolExecutor 与过载治理

> 对应练习：JucLab07ThreadPoolFactory

### 为什么使用线程池

线程池复用线程并控制资源上限，还提供队列、拒绝、关闭和监控能力。它解决的是线程生命周期与资源治理，不会自动让不安全的业务代码变安全。

### 七个核心构造参数

```java
new ThreadPoolExecutor(
        corePoolSize,
        maximumPoolSize,
        keepAliveTime,
        timeUnit,
        workQueue,
        threadFactory,
        rejectionHandler);
```

`corePoolSize`

- 核心线程数。
- 默认情况下即使空闲也保留。
- 可以通过 `allowCoreThreadTimeOut(true)` 允许核心线程超时。

`maximumPoolSize`

- 线程池允许创建的最大线程数。
- 只有选用的队列策略允许扩容时，这个参数才真正发挥作用。

`keepAliveTime` 与 `timeUnit`

- 非核心空闲线程存活时间及其时间单位。
- 例如 `30L, TimeUnit.SECONDS`。

`workQueue`

- 核心线程都忙时保存等待任务。
- 有界 ArrayBlockingQueue 能明确限制积压量。

`threadFactory`

- 创建工作线程。
- 应设置可识别的线程名；还可以统一设置未捕获异常处理器。

`rejectionHandler`

- 线程和队列都达到上限时如何处理新任务。
- 过载行为必须是业务设计的一部分。

### execute 的典型决策顺序

```text
当前线程数 < corePoolSize
    -> 创建核心线程执行
否则尝试进入队列
    -> 入队成功：等待
    -> 队列满且线程数 < maximumPoolSize：创建非核心线程
    -> 队列满且线程数已到 maximumPoolSize：拒绝
```

因此，如果使用实际上无界的队列，任务几乎总能入队，
`maximumPoolSize` 往往不会被用到，延迟和内存可能持续增长。

### 四种内置拒绝策略

`AbortPolicy`

- 抛出 `RejectedExecutionException`。
- 失败最明确，调用方可以记录、降级或返回“系统繁忙”。

`CallerRunsPolicy`

- 由提交任务的线程执行。
- 能减慢上游，形成简单背压。
- 如果提交线程是 Web 请求线程，会直接增加请求延迟，必须理解影响。

`DiscardPolicy`

- 静默丢弃。
- 业务任务通常不应无记录地丢失。

`DiscardOldestPolicy`

- 丢弃队列中最旧任务，再尝试提交新任务。
- 只适合明确允许淘汰旧任务的场景。

### 线程数不是越大越好

CPU 密集型任务：

- 线程数通常接近 CPU 核数或核数加一。
- 太多线程只会增加上下文切换。

I/O 密集型任务：

- 线程等待 I/O 时不占用 CPU，可以配置更多线程。
- 粗略思路是结合等待时间与计算时间估算，但最终必须压测。
- 还必须受数据库连接数、HTTP 连接池和下游容量约束。

不要只根据本机 CPU 设置线程数。某个服务即使有 200 个工作线程，如果数据库连接池只有 20，额外请求只是在别处排队。

### 队列容量

容量过小：

- 流量轻微波动就拒绝。
- 但失败快，尾延迟更可控。

容量过大：

- 能吸收短时突发。
- 持续过载时任务等待很久，占用内存，请求可能尚未执行就已经超过业务超时。

合理容量来自：

- 可接受的排队时间。
- 峰值流量。
- 单任务平均和尾部耗时。
- 单个任务对象占用。
- 下游容量和拒绝后的降级策略。

### Executors 快捷工厂的注意点

`Executors.newFixedThreadPool` 默认使用近似无界的 LinkedBlockingQueue，持续过载时可能大量积压。

`Executors.newCachedThreadPool` 最大线程数非常大，阻塞任务突增时可能创建过多线程。

快捷工厂适合清楚边界的小工具。生产服务通常显式创建 ThreadPoolExecutor，让容量、线程名和拒绝策略可见。

### 提交方式

`execute(Runnable)`

- 没有 Future 返回值。
- 未捕获运行时异常会到达工作线程的未捕获异常处理流程。

`submit(Runnable/Callable)`

- 返回 Future。
- 任务异常被保存在 Future 中；如果从不调用 `get()`，异常容易被忽略。

### 优雅关闭

```java
executor.shutdown();
```

停止接收新任务，已经提交的任务继续执行。

```java
executor.shutdownNow();
```

尝试中断正在执行的任务，并返回尚未开始的队列任务。它也不是强制终止；任务必须正确响应中断。

标准流程：

```java
executor.shutdown();
if (!executor.awaitTermination(
        timeout,
        unit)) {
    executor.shutdownNow();
}
```

当前等待线程被中断时，应调用 shutdownNow 并恢复中断标记。

### 需要监控的指标

- `getPoolSize()`：当前工作线程数。
- `getActiveCount()`：近似活跃线程数。
- `getQueue().size()`：当前排队任务数。
- `getCompletedTaskCount()`：近似已完成任务数。
- 拒绝任务数：通常在自定义拒绝策略中记录。
- 任务等待时间、执行时间、成功率和超时率。

### ThreadLocal 泄漏

线程池线程会长期复用。一次请求放入 ThreadLocal 的数据可能残留到下一次任务：

```java
try {
    context.set(requestId);
    process();
} finally {
    context.remove();
}
```

必须在 finally 中 remove。

---

## Lab 08｜秒杀库存与请求幂等

> 对应练习：JucLab08FlashSaleService

### 两个独立问题

防超卖：

- 100 件库存最多只能有 100 次成功扣减。
- 剩余库存不能小于 0。

请求幂等：

- 同一个 requestId 因网络重试被调用多次时，只产生一次业务结果。
- 重复调用应该返回第一次的结果，而不是再次扣库存。

只实现 CAS 不能阻止同一个请求 ID 重复购买；只用 Set 记录请求也不能保证库存扣减安全。

### computeIfAbsent 建立幂等结果

```java
return results.computeIfAbsent(
        requestId,
        this::reserveOne);
```

第一次出现的 requestId 执行 `reserveOne` 并保存结果。后续相同 ID 直接得到已保存结果。

保存完整结果比只保存“处理过”更有用。重复请求能够得到 SUCCESS 或 SOLD_OUT 的原始业务状态。

### CAS 防止超卖

```java
while (true) {
    int current = stock.get();
    if (current == 0) {
        return soldOut(requestId);
    }
    if (stock.compareAndSet(
            current,
            current - 1)) {
        return success(requestId);
    }
}
```

关键不变式：

```text
remainingStock >= 0
成功结果数 <= 初始库存
相同 requestId 的业务结果不变化
```

### 为什么练习只保证单 JVM

JUC 对象只存在于当前 JVM 内存。服务部署多个实例后：

```text
实例 A 有自己的 AtomicInteger
实例 B 有自己的 AtomicInteger
```

两个实例无法通过本地 CAS 协调同一份数据库库存。

真实分布式系统常见方案：

- 数据库条件更新：

```sql
UPDATE inventory
SET stock = stock - 1
WHERE sku = ?
  AND stock > 0;
```

根据受影响行数判断是否成功。

- 数据库唯一索引约束 requestId 或订单业务键，实现最终幂等防线。
- Redis Lua 脚本在 Redis 内原子检查和扣减。
- 消息队列削峰，消费者串行或分区处理同一商品。
- 分布式锁用于必须跨节点互斥的临界区，但要考虑租约、续期、主从切换和 fencing token。

这些方案仍需处理数据库事务、消息重复、超时后的未知结果和补偿。

### 典型高并发入口

```text
请求
  -> 身份验证
  -> 请求幂等检查
  -> 限流
  -> 有界线程池或消息队列
  -> 库存条件扣减
  -> 创建订单
  -> 返回或异步通知结果
```

顺序会根据业务调整，但每一层都应回答：

- 容量上限是什么？
- 超时后返回什么？
- 重试会不会重复执行？
- 服务重启后状态是否还在？
- 多实例之间由谁协调？

---

## 生产实践｜如何证明并发代码可靠

### 普通单元测试不够

并发错误依赖特定交错，运行一次通过不能证明安全。可以组合：

- `CountDownLatch` 同时放行多个线程，扩大竞争。
- 循环执行并发测试。
- 验证业务不变式，而不只验证某一次执行顺序。
- 设置超时，避免死锁测试永远挂起。
- 压力测试观察吞吐、P99、错误和拒绝。
- 使用 JMH 做微基准，避免手写计时受到 JVM 预热和优化干扰。

### 不要用 sleep 保证顺序

```java
Thread.sleep(100);
assertTaskFinished();
```

机器负载变化后可能失败。优先使用 join、Future.get、CountDownLatch 或其他明确同步机制。

sleep 适合模拟耗时，不适合证明某事件已经发生。

### 线程转储

线程转储可以看到：

- 线程名称和状态。
- 正在执行的调用栈。
- BLOCKED 或 WAITING 在哪个锁或条件上。
- JVM 检测到的 Java 层死锁。

清晰的线程名会直接提升排障效率，这也是 Lab 01 和 Lab 07 要求命名的原因。

### 常见故障检查表

1. 是否把 `run()` 当成 `start()` 使用？
2. 是否吞掉了 InterruptedException？
3. `count++` 是否被误认为原子操作？
4. volatile 是否被误用于复合更新？
5. lock、permit、连接等资源是否在 finally 释放？
6. 是否在锁内调用慢数据库或远程接口？
7. ConcurrentHashMap 的多个方法是否被错误组合成非原子逻辑？
8. BlockingQueue 和线程池队列是否有明确上限？
9. 拒绝策略是否会静默丢失业务任务？
10. submit 返回的 Future 是否有人观察异常？
11. ThreadLocal 是否在 finally 中 remove？
12. 本地锁是否被误认为能协调多个 JVM 实例？
13. 重试是否有 requestId 和持久化幂等约束？
14. 是否只优化平均延迟，而忽略 P99 和排队时间？

### 面试和工作中更重要的表达方式

回答并发设计题时，可以按以下顺序说明：

1. 明确共享状态和业务不变式。
2. 说明竞争发生在哪一步。
3. 选择原子类、锁、并发容器或消息传递。
4. 定义线程池、队列、超时和拒绝的边界。
5. 说明中断、异常、重试和关闭行为。
6. 说明单 JVM 与分布式部署的差异。
7. 给出验证方法和监控指标。

会背 API 是起点；能够说明正确性、容量边界和失败行为，才是高并发工程能力。
