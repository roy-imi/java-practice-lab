# JUC Lab 09 提示

## 提示 1

基础方法分别对应：

```text
set     -> ThreadLocal.set
current -> ThreadLocal.get + Optional.ofNullable
clear   -> ThreadLocal.remove
```

## 提示 2

`wrap` 必须在返回 Lambda 之前读取一次 `CURRENT.get()`。这一步发生在提交任务的调用线程中，得到需要传播的 `captured`。

## 提示 3

返回的 Lambda 中使用以下结构：

```text
保存工作线程 previous
try:
    安装 captured
    运行 task
finally:
    previous 为空就 remove
    previous 不为空就重新 set
```

不能只在 finally 中无条件 remove，否则嵌套调用会破坏外层上下文。
