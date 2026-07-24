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
| Lab 09 | `JucLab09ThreadLocalContext` | ThreadLocal、请求上下文、线程池传播、恢复与清理 |
| Lab 10 | `JucLab10SynchronizedInventory`、`JucLab10LockInventory` | synchronized、ReentrantLock、Condition |
| Lab 11 | `JucLab11VolatileServiceState` | volatile、可见性、安全发布、原子性边界 |

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

### 线程状态转换图

下面是线程从创建到结束的主干：

```text
                 start()
    NEW --------------------------> RUNNABLE
                                      |
                                      | run() 正常结束
                                      | 或抛出未捕获异常
                                      v
                                  TERMINATED
```

RUNNABLE 与阻塞、等待状态之间的转换：

```text
一、等待 synchronized 监视器

┌────────────┐   进入 synchronized，但监视器被占用   ┌─────────┐
│  RUNNABLE  │ ───────────────────────────────────> │ BLOCKED │
└────────────┘ <─────────────────────────────────── └─────────┘
                         获得监视器


二、无限期等待

┌────────────┐   join()、park()、Latch.await()       ┌─────────┐
│  RUNNABLE  │ ───────────────────────────────────> │ WAITING │
└────────────┘ <─────────────────────────────────── └─────────┘
               目标线程结束、unpark、条件满足、
               countDown 至 0、interrupt


三、有限期等待

┌────────────┐   sleep(t)、join(t)、                  ┌───────────────┐
│  RUNNABLE  │   parkNanos(t)、带超时的 await         │ TIMED_WAITING │
│            │ ────────────────────────────────────> │               │
└────────────┘ <──────────────────────────────────── └───────────────┘
                 超时、收到信号、条件满足、interrupt


Object.wait() 的特殊返回路径

┌─────────────────────────┐
│ WAITING 或 TIMED_WAITING │
└────────────┬────────────┘
             │ notify、超时或 interrupt
             v
       ┌─────────┐
       │ BLOCKED │  等待重新获得 synchronized 监视器
       └────┬────┘
            │ 获得监视器
            v
      ┌────────────┐
      │  RUNNABLE  │
      └────────────┘
```

`park`、Latch 或 Semaphore 等等待条件满足后，线程会重新具备运行资格；
`Object.wait()` 必须先重新取得同一个对象的 synchronized 监视器，才能
真正从 wait 方法返回。

### 使用 getState() 观察状态

`thread.getState()`：

- 参数：没有参数。
- 返回值：调用瞬间的 `Thread.State`。
- 它只是快照，线程可能在方法返回后立刻切换状态。

```java
Thread worker = new Thread(
        () -> {
            try {
                Thread.sleep(1_000);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
        },
        "state-demo");

System.out.println(worker.getState());
// NEW

worker.start();
Thread.sleep(50);
System.out.println(worker.getState());
// 大概率为 TIMED_WAITING

worker.join();
System.out.println(worker.getState());
// TERMINATED
```

示例中的第二次输出使用 sleep 只是为了方便观察，不能把“大概率”状态用于严格业务判断。并发测试应使用 Latch、Barrier 等同步工具建立确定的事件关系。

### 方法与状态变化对照

| 方法或事件 | 调用前 | 典型状态变化 | 关键说明 |
| --- | --- | --- | --- |
| `new Thread(task)` | 不存在线程对象 | → NEW | 只创建对象，不执行任务 |
| `thread.start()` | NEW | NEW → RUNNABLE | JVM 启动新线程；不能调用第二次 |
| 调度器暂时没有分配 CPU | RUNNABLE | 仍是 RUNNABLE | Java 状态不区分“正在运行”和“等待 CPU” |
| `Thread.yield()` | RUNNABLE | 通常仍是 RUNNABLE | 只是调度提示，不保证其他线程立即运行 |
| 进入被占用的 `synchronized` | RUNNABLE | → BLOCKED | 专门等待对象监视器；不能靠 interrupt 退出等待 |
| 获得 synchronized 监视器 | BLOCKED | → RUNNABLE | 获得锁后继续执行临界区 |
| `Object.wait()` | RUNNABLE | → WAITING | 必须持有该对象监视器；调用时释放它 |
| `Object.wait(timeout)` | RUNNABLE | → TIMED_WAITING | 释放对象监视器；超时或被唤醒后还要重新竞争监视器 |
| `Thread.sleep(timeout)` | RUNNABLE | → TIMED_WAITING | 不会释放已经持有的 synchronized 或 Lock |
| `thread.join()` | RUNNABLE | → WAITING | 等待目标线程 TERMINATED；当前线程可被中断 |
| `thread.join(timeout)` | RUNNABLE | → TIMED_WAITING | 目标线程结束或超时后返回 |
| `LockSupport.park()` | RUNNABLE | → WAITING | 不释放已经持有的锁；unpark 或 interrupt 后返回 |
| `LockSupport.parkNanos(...)` | RUNNABLE | → TIMED_WAITING | 超时、unpark 或 interrupt 后返回 |
| `CountDownLatch.await()` | RUNNABLE | → WAITING | 计数归零后回到 RUNNABLE |
| `CountDownLatch.await(timeout, unit)` | RUNNABLE | → TIMED_WAITING | 返回值表示计数是否在超时前归零 |
| `Semaphore.acquire()` | RUNNABLE | → WAITING | 没有许可时等待；获得许可后回到 RUNNABLE |
| `BlockingQueue.put()` | RUNNABLE | → WAITING | 有界队列已满时等待 |
| `BlockingQueue.take()` | RUNNABLE | → WAITING | 队列为空时等待 |
| `Future.get()` | RUNNABLE | → WAITING | Future 完成后回到 RUNNABLE |
| `Future.get(timeout, unit)` | RUNNABLE | → TIMED_WAITING | 完成、超时或中断后结束等待 |
| `run()` 返回或未捕获异常终止 | RUNNABLE | → TERMINATED | TERMINATED 的 Thread 不能再次 start |

表中的 WAITING 是“确实需要等待时”的典型状态。例如队列不满时调用 `put()` 可以立即完成，不会进入 WAITING。

### wait、sleep 与 join 的区别

| 对比项 | `Object.wait()` | `Thread.sleep()` | `Thread.join()` |
| --- | --- | --- | --- |
| 用途 | 等待某个共享条件变化 | 暂停当前线程一段时间 | 等待另一个线程结束 |
| 是否必须在 synchronized 中 | 是，必须持有目标对象监视器 | 否 | 否 |
| 是否释放 synchronized 监视器 | 释放调用 wait 的对象监视器 | 不释放任何已持有锁 | 不释放调用方已经持有的业务锁 |
| 无超时状态 | WAITING | 没有无超时版本 | WAITING |
| 有超时状态 | TIMED_WAITING | TIMED_WAITING | TIMED_WAITING |
| 是否响应 interrupt | 是，抛 InterruptedException | 是，抛 InterruptedException | 是，抛 InterruptedException |

### notify 后为什么不一定立即运行

```java
synchronized (lock) {
    lock.notifyAll();
    // 被唤醒线程此时还不能进入 synchronized 代码
    doMoreWork();
}
```

`notify()` 或 `notifyAll()` 只把等待线程从等待集合中唤醒，不会立即释放监视器。被唤醒线程必须等当前线程退出 synchronized，重新获得监视器后才能继续：

```text
WAITING
   -- notify / notifyAll -->
BLOCKED
   -- 重新获得监视器 -->
RUNNABLE
```

### interrupt 对不同状态的影响

- 对 `sleep`、`wait`、`join` 和多数可中断 JUC 等待：结束等待并抛出 `InterruptedException`，异常抛出时中断标记通常被清除。
- 对 `LockSupport.park`：park 返回，不抛 InterruptedException，中断标记保留。
- 对正在等待 synchronized 监视器的 BLOCKED 线程：只设置中断标记，不会让它退出锁竞争。
- 对普通 RUNNABLE 计算：设置中断标记，代码必须通过 `isInterrupted()` 主动检查。
- `interrupt()` 不会让线程直接进入 TERMINATED。

### JUC 锁等待为什么通常不是 BLOCKED

BLOCKED 主要表示等待 Java 内置对象监视器，也就是进入 `synchronized` 时的锁竞争。

`ReentrantLock`、`CountDownLatch`、`Semaphore` 等 JUC 工具通常基于 AQS 和 `LockSupport.park()` 实现，因此竞争时常看到 WAITING 或 TIMED_WAITING，而不是 BLOCKED。

另外，某些操作系统 I/O 阻塞在 Java 的 `Thread.State` 中仍可能显示 RUNNABLE。Java 线程状态是 JVM 视角，不等同于操作系统线程的全部状态。

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

volatile 的内存语义、安全发布和专项练习见 Lab 11。

---

## Lab 03｜显式锁与读多写少缓存

> 对应练习：JucLab03PriceCache

本课重点是 `ReentrantReadWriteLock`。`synchronized`、独占
`ReentrantLock` 和 `Condition` 的系统对比及专项练习见 Lab 10。

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

### 线程池与 ThreadLocal

线程池线程会长期复用。一次请求放入 ThreadLocal 的数据可能残留到下一次任务：

```java
try {
    context.set(requestId);
    process();
} finally {
    context.remove();
}
```

必须在 finally 中 remove。ThreadLocal 的日常场景、内部结构、线程池传播和配套练习见 Lab 09。

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

## Lab 09｜ThreadLocal 与请求上下文

> 对应练习：JucLab09ThreadLocalContext

`ThreadLocal` 位于 `java.lang`，不是 `java.util.concurrent` 包中的类，但它是
Java 并发编程和线程池上下文管理中的核心工具，因此放在 JUC 课程中学习。

### ThreadLocal 解决什么问题

ThreadLocal 为“每个线程”保存一份独立变量。多个线程访问同一个
ThreadLocal 对象时，读写的是各自线程中的值：

```text
                         同一个 ThreadLocal<RequestContext>
                                      |
                 +--------------------+--------------------+
                 |                                         |
                 v                                         v
        Thread request-1                          Thread request-2
        requestId = R-1001                        requestId = R-1002
        userId    = U-01                          userId    = U-02
```

因此，业务方法不必层层传递 requestId，也不会因为另一个线程调用
`set()` 就覆盖当前线程的值。

ThreadLocal 不是用于线程之间共享数据。它恰恰是把状态限制在线程内部，
减少共享。

### 日常使用场景

#### 请求链路和日志上下文

Web 请求进入时保存 requestId、traceId、用户 ID，业务深层代码和日志工具
可以直接读取：

```java
private static final ThreadLocal<String> TRACE_ID =
        new ThreadLocal<>();

try {
    TRACE_ID.set("trace-1001");
    callService();
} finally {
    TRACE_ID.remove();
}
```

日志框架的 MDC 也常用于类似目的。MDC 的具体实现由日志框架提供，但在线程池
中的传播和清理问题与 ThreadLocal 很相似。

#### 当前登录用户或权限上下文

认证过滤器可以把已经验证的用户信息绑定到请求线程，后续代码读取当前用户：

```java
RequestContext context =
        new RequestContext(
                "request-1001",
                "user-42");
```

这里适合保存小型、不可变的上下文，不应把完整请求对象、大量缓存数据长期放入
ThreadLocal。

#### 框架绑定的事务和资源

一些框架会把事务状态、数据库会话或资源句柄绑定到当前线程，使同一调用链中的
代码共享它们。

这类资源通常不能随意传播到异步线程。数据库 Connection 和事务上下文一般不是
为了多线程并行使用而设计的。把普通追踪 ID 传播到线程池是合理的，不代表事务
对象也能同样传播。

#### 旧式非线程安全工具

早期代码可能为每个线程保存一份 `SimpleDateFormat`：

```java
private static final ThreadLocal<SimpleDateFormat> FORMATTER =
        ThreadLocal.withInitial(
                () -> new SimpleDateFormat("yyyy-MM-dd"));
```

Java 8 新代码更推荐使用不可变且线程安全的
`java.time.format.DateTimeFormatter`，不再需要 ThreadLocal。

#### 每线程缓冲区

高频编码或解析场景有时会复用每线程缓冲区，减少重复创建对象。但要严格控制
缓冲区最大容量，否则线程池中的每个长期线程都可能保留一份大对象。

随机数场景不需要自己创建 `ThreadLocal<Random>`，JUC 已提供
`ThreadLocalRandom.current()`。

### 不适合使用 ThreadLocal 的场景

- 多个线程需要共同更新的计数、库存或缓存。
- 必须跨进程、跨服务保存的数据。
- 希望异步任务自动继承请求数据，但没有设计传播机制。
- 生命周期不清楚的大对象或资源。
- 可以通过普通方法参数清楚表达的数据。

显式参数更容易测试和理解。ThreadLocal 更适合横跨多层调用、又确实属于当前
执行上下文的少量信息。

### new ThreadLocal()

```java
ThreadLocal<String> currentUser =
        new ThreadLocal<>();
```

- 构造参数：没有参数。
- 新线程第一次 `get()` 时默认返回 null。
- ThreadLocal 对象本身通常定义为 `private static final`，但值仍然按线程隔离。

```java
System.out.println(currentUser.get());
// null
```

### set(value)

- `value`：要保存到当前线程中的值。
- 返回值：void。
- 只改变当前线程对应的条目。

```java
currentUser.set("user-42");
System.out.println(currentUser.get());
// user-42
```

线程 A 调用 set，不会让线程 B 的 get 返回这个值。

### get()

- 参数：没有参数。
- 返回值：当前线程对应的值。
- 当前线程没有值时，调用 `initialValue()` 得到初始值。

```java
String userId = currentUser.get();
```

没有重写 `initialValue()`、也没有使用 `withInitial` 时，初始值是 null。

### remove()

- 参数：没有参数。
- 返回值：void。
- 删除当前线程在这个 ThreadLocal 中的条目。

```java
currentUser.remove();
System.out.println(currentUser.get());
// null
```

`set(null)` 与 `remove()` 不完全相同：

- `set(null)` 仍可能保留一个 value 为 null 的条目。
- `remove()` 删除条目，也让下次 get 可以重新执行初始化逻辑。

请求结束时应使用 remove。

### withInitial(supplier)

```java
ThreadLocal<List<String>> messages =
        ThreadLocal.withInitial(ArrayList::new);
```

- `supplier`：当前线程第一次 get 时负责创建初始值的 Supplier。
- 返回值：带延迟初始化逻辑的 ThreadLocal。
- 每个线程分别执行自己的初始化。

```java
messages.get().add("hello");
System.out.println(messages.get());
// [hello]

messages.remove();
System.out.println(messages.get());
// []，remove 后再次 get，重新创建 List
```

Supplier 可能在不同线程分别执行，因此不要让它偷偷修改不安全的共享状态。

### initialValue()

也可以通过继承重写初始值：

```java
ThreadLocal<Integer> retryCount =
        new ThreadLocal<Integer>() {
            @Override
            protected Integer initialValue() {
                return 0;
            }
        };
```

Java 8 中简单初始化通常优先使用 `withInitial`，代码更紧凑。

### 请求入口的标准生命周期

ThreadLocal 的使用必须有清晰边界：

```text
请求进入
   -> set(context)
   -> 执行业务
   -> finally remove()
请求结束
```

代码结构：

```java
try {
    requestContext.set(context);
    filterChain.doFilter(request, response);
} finally {
    requestContext.remove();
}
```

清理放在 finally 中，因为业务异常、超时或提前返回都不能跳过它。

### 线程池复用为什么会串号

假设线程池只有一个工作线程：

```text
pool-1-thread-1 执行请求 A
    -> set(user-A)
    -> 忘记 remove

pool-1-thread-1 随后执行请求 B
    -> B 没有 set
    -> get() 读到 user-A
```

ThreadLocal 的隔离单位是线程，不是请求。线程池让同一线程先后执行多个请求，
所以“每线程一份”会变成“多个请求先后共用一份”。

这会造成两类问题：

- 数据污染：请求 B 错误读取请求 A 的用户或 traceId。
- 内存滞留：长期线程一直引用旧值，大对象不能及时回收。

### 异步任务不会自动得到调用方上下文

```java
requestContext.set(context);

executor.execute(() -> {
    // 这里在另一个线程中
    System.out.println(requestContext.get());
    // 默认是 null，或者更危险地读到该工作线程残留的旧值
});
```

ThreadLocal 不会随着 Runnable 自动传递。安全传播需要三个阶段：

```text
提交任务的线程：捕获 captured
        ↓
工作线程执行前：保存 previous，安装 captured
        ↓
finally：恢复 previous；previous 为空则 remove
```

为什么恢复 previous，而不是无条件 remove：

```text
外层上下文 A
    -> 执行临时上下文 B
    -> B 结束后应该恢复 A
```

无条件 remove 会把外层 A 一起清掉，嵌套调用就不安全。

### 上下文包装器的通用结构

下面是算法结构，不使用本练习的类名：

```java
Context captured = local.get();

return () -> {
    Context previous = local.get();
    try {
        install(captured);
        task.run();
    } finally {
        install(previous);
    }
};
```

`install(null)` 应调用 remove，非 null 才调用 set。

捕获动作必须发生在提交线程调用包装器时，不能等到工作线程运行后才捕获，否则
读到的是工作线程自己的上下文。

### InheritableThreadLocal

`InheritableThreadLocal` 会在线程创建时，把父线程当时的值提供给子线程：

```java
InheritableThreadLocal<String> user =
        new InheritableThreadLocal<>();

user.set("user-42");
new Thread(() ->
        System.out.println(user.get()))
        .start();
// user-42
```

它不适合解决普通线程池传播：

- 线程池工作线程通常早已创建，不会在每次提交任务时重新继承。
- 工作线程会执行多个不同请求，继承一次的旧值可能持续存在。
- 默认传递的是同一个对象引用，不是深拷贝；可变对象仍可能被父子线程共同修改。

因此，请求上下文在线程池中应按“每个任务”捕获、安装和恢复，不能仅换成
InheritableThreadLocal。

### ThreadLocalMap 内部关系

每个 Thread 内部维护自己的 ThreadLocalMap：

```text
Thread
  |
  +-- threadLocals: ThreadLocalMap
          |
          +-- Entry
                key   -> ThreadLocal 对象的弱引用
                value -> 业务值的强引用
```

ThreadLocal 对象只是查找当前线程 Map 中条目的 key。真正的数据存放在线程对象
内部，因此同一个 ThreadLocal 在不同线程中会找到不同 Map 和不同 value。

### 弱引用 key 为什么仍可能内存滞留

ThreadLocalMap 的 key 使用弱引用。如果外部已经没有 ThreadLocal 的强引用，
key 可能被垃圾回收并变成 null；但是 Entry 对 value 仍然是强引用。

```text
key = null
value = 大对象，仍被工作线程引用
```

ThreadLocalMap 会在后续部分操作中顺便清理陈旧条目，但清理时机不能被当作业务
保证。线程池线程可能存活很久，所以最可靠的办法仍是 finally 中主动 remove。

即使 ThreadLocal 定义为 static final、key 不会消失，也仍要 remove：否则旧请求
值会一直留在线程里，并污染下一任务。

### ThreadLocal 保证了什么

ThreadLocal 保证的是线程隔离访问方式，不会自动保证 value 本身线程安全。

如果把同一个可变对象同时 set 到多个线程，或者又通过全局变量共享该对象，仍然
存在数据竞争。最安全的请求上下文通常设计为不可变对象。

### 与 CompletableFuture 配合

`CompletableFuture.supplyAsync`、`thenApplyAsync` 等异步阶段可能运行在其他
线程，普通 ThreadLocal 不会自动跟随：

```java
Runnable contextAwareTask =
        wrap(this::process);

CompletableFuture.runAsync(
        contextAwareTask,
        businessExecutor);
```

每一个可能切换线程的异步边界都要明确由谁传播上下文。实际项目也可以使用日志
框架、链路追踪库或经过评估的上下文传播组件，但仍要理解捕获、安装和恢复的
生命周期。

### Lab 09 要保持的不变式

1. 同一线程 set 后能够读取。
2. 其他线程默认读不到调用方上下文。
3. 包装任务读取到提交线程捕获的上下文。
4. 工作线程原来的上下文在任务结束后得到恢复。
5. task 抛异常也必须恢复。
6. 原上下文为空时必须 remove，不能留下请求数据。

### ThreadLocal 检查表

1. 这个数据真的属于当前线程或当前请求吗？
2. 是否可以用明确的方法参数代替？
3. 请求入口是否 set，finally 是否 remove？
4. 线程池任务是否按任务捕获和恢复？
5. 是否错误依赖 InheritableThreadLocal 传播线程池上下文？
6. 是否保存了大对象、连接或可变共享对象？
7. 异常、取消和超时时是否仍会清理？
8. 单元测试是否验证了线程复用和异常路径？

---

## Lab 10｜synchronized、ReentrantLock 与 Condition

> 对应练习：JucLab10SynchronizedInventory、JucLab10LockInventory

### 锁为什么属于并发核心

多个线程访问共享可变状态时，业务通常不只要求“某个字段能被看见”，还要求一组
操作不可被其他线程插入：

```java
if (stock >= quantity) {
    stock -= quantity;
}
```

检查和扣减必须作为一个整体。否则两个线程可能同时通过检查，造成超卖。

锁建立临界区，同一时刻只允许持有同一把锁的线程进入。它通常同时提供：

- 互斥：其他线程不能进入同一锁保护的临界区。
- 原子性：临界区中的组合操作不会与另一临界区交错。
- 可见性：释放锁前的写入，对后续获得同一把锁的线程可见。
- 有序性：锁边界建立 Java 内存模型中的 happens-before 关系。

锁保护的不是某一行代码，而是业务不变式。例如：

```text
remainingStock >= 0
账户 A + 账户 B 的总金额不变
队列元素数量始终在 0 到 capacity 之间
```

所有读写这个不变式的代码都必须使用同一把锁。写操作加锁、读取却不加锁，仍然
可能读到不一致状态。

### synchronized 的三种形式

#### 同步实例方法

```java
public synchronized void update() {
    // 临界区
}
```

锁对象是当前实例 `this`。两个线程调用同一个实例时互斥；调用不同实例时使用
不同锁，不会互斥。

等价形式：

```java
public void update() {
    synchronized (this) {
        // 临界区
    }
}
```

#### 同步静态方法

```java
public static synchronized void updateGlobal() {
    // 临界区
}
```

锁对象是声明该方法的 `Class` 对象，例如 `InventoryService.class`，不是某个
业务实例。

等价形式：

```java
public static void updateGlobal() {
    synchronized (InventoryService.class) {
        // 临界区
    }
}
```

#### 同步代码块

```java
private final Object monitor = new Object();

public void update() {
    synchronized (monitor) {
        // 只锁真正需要保护的部分
    }
}
```

同步代码块可以缩小锁范围，也能避免外部代码获得 `this` 后参与锁竞争。

推荐监视器通常是：

```java
private final Object monitor = new Object();
```

不推荐使用可被外部访问或复用的对象，例如字符串常量、装箱整数和公开对象：

```java
synchronized ("LOCK") {
    // 字符串常量可能被其他无关代码共享
}
```

表达式计算结果为 null 时，进入 synchronized 会抛
`NullPointerException`。

### 对象监视器

每个 Java 对象都可以作为监视器。线程进入 synchronized 代码前必须获得该
对象的监视器：

```text
线程 A 获得 monitor
    -> 进入临界区

线程 B 尝试获得同一个 monitor
    -> BLOCKED
    -> A 退出后再竞争 monitor
```

只有竞争同一个对象才会互斥：

```java
synchronized (lockA) {
    updateStock();
}

synchronized (lockB) {
    readStock();
}
```

如果 lockA 和 lockB 不是同一个对象，这两段代码不能共同保护 stock。

### synchronized 自动释放锁

线程离开 synchronized 代码块时，JVM 自动释放监视器：

- 正常执行到代码块末尾。
- return 提前返回。
- 抛出异常。

因此 synchronized 不需要手写 unlock：

```java
synchronized (monitor) {
    if (invalid()) {
        return;
    }
    update();
}
```

即使 update 抛异常，监视器也会释放。

但“自动释放锁”不代表业务操作会自动回滚。已经修改了一半的数据仍需事务或补偿
机制维护一致性。

### 可重入

synchronized 和 ReentrantLock 都是可重入锁。线程已经持有某把锁时，可以再次
获得同一把锁：

```java
public synchronized void outer() {
    inner();
}

public synchronized void inner() {
    // 同一线程可以再次进入
}
```

锁内部维护持有次数。每次成功进入都要有对应退出，持有次数归零后其他线程才能
获得锁。

### synchronized 与 happens-before

线程 A 退出某个 synchronized 块，相当于释放监视器；线程 B 随后获得同一个
监视器。A 在释放前的操作对 B 可见：

```text
线程 A：修改库存 -> 退出 synchronized
                           |
                           | happens-before
                           v
线程 B：进入同一 monitor 的 synchronized -> 读取库存
```

关键是“同一 monitor”。使用另一把锁无法建立这条关系。

### Object.wait()

`wait` 是 `Object` 的方法，不是 Thread 的方法：

```java
monitor.wait();
monitor.wait(timeoutMillis);
monitor.wait(timeoutMillis, nanos);
```

调用要求：

- 当前线程必须已经持有 monitor 的监视器。
- 不满足时抛 `IllegalMonitorStateException`。
- wait 会释放这个 monitor，使其他线程能够修改条件。
- 被唤醒后必须重新获得 monitor，才能从 wait 返回。
- wait 可以被中断并抛出 `InterruptedException`。

正确的条件等待必须使用 while：

```java
synchronized (monitor) {
    while (!conditionIsTrue()) {
        monitor.wait();
    }
    changeSharedState();
}
```

不能只使用 if：

```java
if (!conditionIsTrue()) {
    monitor.wait();
}
```

原因：

- 线程可能无理由地从 wait 返回，这叫虚假唤醒。
- notifyAll 会唤醒多个线程，第一个线程可能已经消耗了条件。
- 从 wait 返回前需要重新竞争锁，期间条件可能再次变化。

所以“被唤醒”只表示应该重新检查，不表示业务条件一定成立。

### notify() 与 notifyAll()

```java
synchronized (monitor) {
    changeCondition();
    monitor.notifyAll();
}
```

`notify()`：

- 随机选择一个等待同一 monitor 的线程唤醒。
- 如果多个不同业务条件共用一个 monitor，可能唤醒不合适的线程。

`notifyAll()`：

- 唤醒所有等待同一 monitor 的线程。
- 每个线程重新竞争锁，并在 while 中检查自己的条件。
- 调度成本可能更高，但简单条件协调通常更不容易遗漏进展。

二者都要求调用线程持有 monitor，也都不会立即释放锁。真正释放发生在退出
synchronized 后。

### wait 的超时计算

一次等待可能因为通知或虚假唤醒提前返回，因此带超时的业务不能每次重新等待
完整 timeout：

```text
总预算 1 秒
第一次等待 300ms 后提前醒来
剩余预算应为约 700ms
```

推荐使用 `System.nanoTime()` 计算经过时间：

```java
long remaining = unit.toNanos(timeout);
long deadline = System.nanoTime() + remaining;

while (!conditionIsTrue()) {
    if (remaining <= 0L) {
        return false;
    }
    // wait 剩余时间
    remaining = deadline - System.nanoTime();
}
```

`System.nanoTime()` 适合计算时间间隔，不代表现实世界日期时间。

### ReentrantLock

```java
ReentrantLock lock = new ReentrantLock();
```

ReentrantLock 与 synchronized 都能提供可重入互斥和内存可见性。它额外提供：

- 可中断获得锁。
- 非阻塞尝试获得锁。
- 带超时获得锁。
- 可选公平策略。
- 一个 Lock 创建多个 Condition。
- 查询锁状态的监控方法。

### ReentrantLock(fair)

```java
new ReentrantLock();
new ReentrantLock(false);
new ReentrantLock(true);
```

- 无参或 false：非公平锁，刚到达的线程可能插队，通常吞吐更高。
- true：公平锁，倾向于让等待更久的线程先获得锁，但调度开销更大。

公平锁不保证业务完成顺序，只影响获得锁的倾向。线程获得锁后执行多久、是否被
操作系统暂停，仍然不可预测。

### lock() 与 unlock()

```java
lock.lock();
try {
    updateSharedState();
} finally {
    lock.unlock();
}
```

`lock()`：

- 参数：没有参数。
- 获得锁后返回。
- 锁被占用时等待。
- 等待期间不能像 `lockInterruptibly` 那样及时以异常退出。

`unlock()`：

- 参数：没有参数。
- 当前线程必须持有该锁。
- 未持有时调用会抛 `IllegalMonitorStateException`。

必须在成功获得锁之后建立 try/finally：

```java
lock.lock();
try {
    // 安全
} finally {
    lock.unlock();
}
```

不要写成：

```java
try {
    lock.lock();
    // 如果获得锁之前出现控制流问题，finally 逻辑很难判断
} finally {
    lock.unlock();
}
```

### lockInterruptibly()

```java
lock.lockInterruptibly();
```

- 参数：没有参数。
- 锁可用时获得锁。
- 等待锁期间收到中断时抛 `InterruptedException`。
- 适合任务取消、服务关闭或避免线程无限等待。

代码结构：

```java
lock.lockInterruptibly();
try {
    process();
} finally {
    lock.unlock();
}
```

如果方法向上抛出 InterruptedException，通常让调用方决定如何处理；如果不能
继续抛，应恢复中断标记。

### tryLock()

立即尝试：

```java
if (lock.tryLock()) {
    try {
        process();
    } finally {
        lock.unlock();
    }
} else {
    rejectOrFallback();
}
```

- 参数：没有参数。
- 返回 true：当前线程获得锁。
- 返回 false：当前没有获得，不等待。

带超时：

```java
if (lock.tryLock(
        100,
        TimeUnit.MILLISECONDS)) {
    try {
        process();
    } finally {
        lock.unlock();
    }
}
```

- 第一个参数：最长等待数量。
- 第二个参数：时间单位。
- 等待时可以响应中断。

注意：公平 ReentrantLock 的无参 `tryLock()` 仍可能插队；如果需要遵守公平等待
策略，可以使用带时间的形式。

### Condition

Condition 是与 Lock 绑定的条件等待队列：

```java
Condition notEmpty = lock.newCondition();
Condition notFull = lock.newCondition();
```

一把锁可以有多个 Condition，例如有界队列可以分别通知“非空”和“未满”，比一个
monitor 上所有线程共用 wait 集合更精确。

`await()`：

```java
lock.lock();
try {
    while (!conditionIsTrue()) {
        condition.await();
    }
    changeSharedState();
} finally {
    lock.unlock();
}
```

- 调用线程必须持有关联 Lock。
- await 会释放 Lock。
- 返回前重新获得 Lock。
- 可以被中断并抛 InterruptedException。
- 同样必须在 while 中检查条件。

`awaitNanos(nanosTimeout)`：

- 参数：最多等待的纳秒数。
- 返回值：估算的剩余纳秒数。
- 返回值小于等于 0 通常表示时间预算用尽。

```java
long remaining = unit.toNanos(timeout);
while (!conditionIsTrue()) {
    if (remaining <= 0L) {
        return false;
    }
    remaining = condition.awaitNanos(remaining);
}
```

`signal()` 和 `signalAll()`：

- 调用线程必须持有关联 Lock。
- signal 唤醒一个等待者。
- signalAll 唤醒全部等待者。
- 被唤醒线程仍需重新获得 Lock。

```java
lock.lock();
try {
    addStock();
    stockAvailable.signalAll();
} finally {
    lock.unlock();
}
```

### synchronized 与 ReentrantLock 对照

| 能力 | synchronized | ReentrantLock |
| --- | --- | --- |
| 可重入 | 支持 | 支持 |
| 异常时释放 | JVM 自动释放 | 必须 finally unlock |
| 等待锁时响应中断 | 不支持退出 monitor 竞争 | `lockInterruptibly` 支持 |
| 尝试获取 | 不支持 | `tryLock` |
| 超时获取 | 不支持 | `tryLock(timeout, unit)` |
| 公平策略 | 不可配置 | 构造器可配置 |
| 条件队列 | 每个 monitor 一个 wait 集合 | 可以创建多个 Condition |
| 状态查询 | 较少 | 提供 isLocked 等方法 |
| 基础语法 | 语言关键字 | JUC 类 |

选择建议：

- 只需要简单互斥时，优先考虑 synchronized，结构不容易忘记释放。
- 需要超时、中断、公平或多个条件队列时，使用 ReentrantLock。
- 不要为了“性能”机械地把 synchronized 全部换成 ReentrantLock。现代 JVM
  对 synchronized 有大量优化，实际性能取决于竞争和临界区。

### 死锁示例

两个转账线程以不同顺序获得账户锁：

```text
线程 1：持有账户 A，等待账户 B
线程 2：持有账户 B，等待账户 A
```

形成循环等待后，两者都无法继续。

预防办法：

- 给锁建立全局顺序，例如始终先锁账户 ID 较小者。
- 避免持锁调用未知外部代码。
- 使用带超时 tryLock，失败后释放已获得的锁并重试。
- 减少同时持有多把锁。
- 不在锁内进行慢网络和数据库调用。

### 锁的粒度

锁范围太大：

- 正确但并发度低。
- 慢操作会让大量线程排队。

锁范围太小：

- 可能把必须原子完成的业务步骤拆开。
- 检查与修改之间重新出现竞态。

正确做法是先确定业务不变式，再让临界区完整覆盖它，然后尽量把无关计算和 I/O
移到锁外。

### Lab 10 的对照关系

两个练习实现完全相同的业务：

```text
库存不足
   -> 等待补货或超时
补货
   -> 唤醒等待者
库存足够
   -> 原子扣减
```

API 对应关系：

| synchronized 版本 | ReentrantLock 版本 |
| --- | --- |
| `synchronized (monitor)` | `lock.lockInterruptibly()` |
| `monitor.wait(...)` | `condition.awaitNanos(...)` |
| `monitor.notifyAll()` | `condition.signalAll()` |
| 退出代码块自动释放 | `finally` 中 `lock.unlock()` |

---

## Lab 11｜volatile、可见性与安全发布

> 对应练习：JucLab11VolatileServiceState

### volatile 要解决的可见性问题

一个线程修改普通字段，另一个线程不一定立即观察到变化：

```java
private boolean running = true;

// 工作线程
while (running) {
    doWork();
}

// 管理线程
running = false;
```

在单线程语义不变的前提下，JIT 可能优化读取，CPU 也有缓存和乱序执行。工作线程
可能长时间继续使用旧值。

增加 volatile：

```java
private volatile boolean running = true;
```

对 running 的写会对后续读取同一 volatile 字段的线程可见。

### volatile 的 happens-before

Java 内存模型规定：

```text
线程 A 对 volatile 字段的写
            |
            | happens-before
            v
线程 B 后续对同一 volatile 字段的读
```

更重要的是，线程 A 在 volatile 写之前完成的普通写，也会在线程 B 读到该新
volatile 值后可见：

```java
data = newData;     // 普通写
ready = true;       // volatile 写
```

另一个线程：

```java
if (ready) {        // volatile 读到 true
    use(data);      // 能看到 volatile 写之前发布的数据
}
```

常见说法“volatile 强制刷新主内存”便于入门，但不够精确。更可靠的理解是：
volatile 读写具有 Java 内存模型规定的可见性和排序语义。

### volatile 能保证什么

- 对该字段的最新写入对其他读取线程可见。
- volatile 写之前的操作不会被重排到写之后。
- volatile 读之后的操作不会被重排到读之前。
- 单次 volatile 读取和写入具有原子性。
- Java 5 之后 volatile long 和 double 的单次读写也不会撕裂。

这里的“单次原子”不包括先读后写的组合。

### volatile 不能保证复合操作原子性

```java
private volatile int count;

count++;
```

`count++` 仍然包含三个步骤：

```text
读取 count
计算 count + 1
写回 count
```

两个线程可能这样交错：

```text
线程 A 读取 10
线程 B 读取 10
线程 A 写入 11
线程 B 写入 11
```

执行两次，结果却只增加一次。

正确选择：

```java
AtomicInteger count = new AtomicInteger();
count.incrementAndGet();
```

或者在需要联合修改多个状态时使用锁：

```java
synchronized (monitor) {
    count++;
    updateRelatedState();
}
```

### volatile 不能保证多字段不变式

```java
private volatile int lower;
private volatile int upper;
```

即使两个字段分别可见，读取线程仍可能看到：

```text
新的 lower + 旧的 upper
```

如果它们必须作为整体变化，可以：

- 使用同一把锁保护读写。
- 创建包含两个值的不可变对象，用一个 volatile 引用整体替换。

### 不可变配置快照

```java
public final class ServiceConfig {
    private final String endpoint;
    private final int timeoutMillis;
}

private volatile ServiceConfig config;
```

更新时创建新对象并一次替换：

```java
config = new ServiceConfig(
        "https://api-v2.example",
        800);
```

读取时先保存一次快照：

```java
ServiceConfig snapshot = config;
call(
        snapshot.getEndpoint(),
        snapshot.getTimeoutMillis());
```

这样同一次业务使用同一个版本，不会 endpoint 取自旧配置、timeout 取自新配置。

### volatile 引用不等于对象内部线程安全

```java
private volatile List<String> users =
        new ArrayList<>();
```

volatile 只作用于 users 这个引用的读取和替换：

```java
users = new ArrayList<>();
```

它不会让下面的操作变安全：

```java
users.add("小明");
```

数组同样如此：volatile 数组引用不代表每个数组元素都是 volatile。

更安全的快照模式是使用不可变对象或不再修改的集合，然后整体替换引用。

### 停止标记

```java
private volatile boolean running = true;

public void runLoop() {
    while (running) {
        processNext();
    }
}

public void requestStop() {
    running = false;
}
```

适合：

- 循环本身会频繁检查标记。
- 工作线程没有永久阻塞在 queue.take、sleep、I/O 等操作。

如果线程可能阻塞，只修改 volatile 标记不能让阻塞方法立即返回。通常还需要
`interrupt()` 或关闭对应资源：

```java
running = false;
worker.interrupt();
```

空转检查标记也会消耗 CPU。能够使用 BlockingQueue、Latch 或条件等待时，不应
为了 volatile 而持续忙等。

### 状态发布

volatile 常用于发布一个已经完整构造好的不可变对象：

```java
private volatile RulesSnapshot rules;

public void reload() {
    RulesSnapshot newRules = loadAndValidate();
    rules = newRules;
}
```

耗时加载和校验在局部变量中完成，最后一次 volatile 写才让其他线程看到整个新
版本。

不要先发布一个可变对象，再逐步填充：

```java
rules = new RulesSnapshot();
rules.loadPartA();
rules.loadPartB();
```

其他线程可能在对象尚未准备好时读到它。

### 双重检查单例

双重检查锁需要 volatile：

```java
private static volatile Service instance;

public static Service getInstance() {
    Service result = instance;
    if (result == null) {
        synchronized (Service.class) {
            result = instance;
            if (result == null) {
                result = new Service();
                instance = result;
            }
        }
    }
    return result;
}
```

volatile 防止发布引用与构造过程发生危险重排，并确保其他线程看到完整对象。

实际项目中，静态初始化或枚举单例通常更简单：

```java
private static final Service INSTANCE =
        new Service();
```

### volatile 与 AtomicReference

volatile 引用适合无条件替换：

```java
config = newConfig;
```

如果更新取决于“当前值仍然是我之前读到的值”，需要条件原子更新：

```java
AtomicReference<ServiceConfig> config =
        new AtomicReference<>(initial);

config.compareAndSet(expected, updated);
```

这与 AtomicInteger 的 CAS 思路相同。

### volatile 与 synchronized、原子类对照

| 需求 | 更适合的工具 |
| --- | --- |
| 一个停止标记的可见性 | volatile |
| 整体替换不可变配置 | volatile 引用 |
| 单个计数器原子递增 | AtomicInteger、LongAdder |
| 基于旧值做条件更新 | Atomic 类 CAS |
| 多字段联合不变式 | synchronized 或 Lock |
| 等待条件变化而不忙等 | wait/Condition/同步器 |
| 限制同一时刻并发数 | Semaphore |

### volatile 的语法边界

volatile 只能修饰成员字段：

```java
private volatile boolean running;
private static volatile ServiceConfig config;
```

不能修饰局部变量或方法。volatile 也不能与 final 同时表达“以后还要更新”，因为
final 字段初始化后不能再次赋值。

### 常见误区

误区一：

```text
加了 volatile，所有线程安全问题就解决了
```

事实：它主要解决可见性和排序，不提供临界区。

误区二：

```text
volatile 比 synchronized 快，所以应该优先替换锁
```

事实：两者解决的问题不同。无法维护正确性时，性能比较没有意义。

误区三：

```text
volatile 引用指向的集合可以安全并发修改
```

事实：引用可见不代表对象内部操作线程安全。

误区四：

```text
用 volatile 停止标记可以唤醒所有阻塞操作
```

事实：阻塞线程通常还需要 interrupt、超时或资源关闭。

### Lab 11 要保持的不变式

1. 管理线程发出停止信号后，循环线程能够观察到。
2. 配置对象在发布前已经完整构造，并且不可变。
3. 读取一次业务操作所需配置时使用同一个快照。
4. 计数操作不使用 `volatile int++`，而使用 AtomicInteger。
5. 不把 volatile 当作多字段事务或互斥锁。

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
