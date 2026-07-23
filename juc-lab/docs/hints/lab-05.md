# JUC Lab 05 提示

## 提示 1

两个方法都返回 Lambda。`waitForStart` 在 Lambda 内先 `await()`，成功后再运行 task。

## 提示 2

捕获 InterruptedException 后调用：

```java
Thread.currentThread().interrupt();
```

## 提示 3

Semaphore 使用 boolean 记录是否真的 acquire 成功。finally 中只有在成功获得许可后才 release。
