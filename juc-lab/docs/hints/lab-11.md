# JUC Lab 11 提示

## 提示 1

首先给 `running` 和 `config` 两个字段添加 `volatile`。volatile 只能修饰字段，不能修饰局部变量。

## 提示 2

停止信号和配置快照都只需要一次赋值或读取。配置必须整体替换不可变 `ServiceConfig`，不要逐字段修改一个共享可变对象。

## 提示 3

请求计数的 `count++` 是复合操作，volatile 不能防止丢失更新。因此使用已经提供的 `AtomicInteger.incrementAndGet()`。
