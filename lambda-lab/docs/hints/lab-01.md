# 第 1 课提示

## 提示 1：先看抽象方法

`IntBinaryOperator.applyAsInt` 有两个 `int` 参数并返回 `int`，所以 Lambda 左边需要两个参数。`Runnable.run` 没有参数也没有返回值，所以左边是空括号。

## 提示 2：表达式体

计算器只有一个算术表达式，可以省略大括号和 `return`。问候任务没有返回值，它的右边只需执行一次 `output.accept(...)`。

## 提示 3：注意执行时机

`greetingTask` 要返回动作，而不是立即执行动作。调用这个方法时不应产生消息；之后调用 `Runnable.run()` 才产生。
