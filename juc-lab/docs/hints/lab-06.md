# JUC Lab 06 提示

## 提示 1

可以定义私有 `WorkItem`，同时保存输入下标和文本。再创建一个下标为 -1 的特殊对象作为毒丸。

## 提示 2

结果可以使用 `AtomicReferenceArray<String>`，消费者根据 WorkItem 的原下标写入。

## 提示 3

推荐顺序：

```text
启动所有消费者
-> put 全部普通任务
-> put 与消费者数量相同的毒丸
-> join 所有消费者
-> 按下标组装 List
```
