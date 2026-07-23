# 第 1 课提示

## 提示 1：先画时间线

价格查询约 360ms，库存查询约 520ms。先后同步调用约 880ms；若先提交两项再等待，整体应接近较慢的 520ms。

## 提示 2：你需要的类型

`ExecutorService.submit(Callable<T>)` 会返回 `Future<T>`。两个服务方法都返回 `int`，装箱后类型是 `Future<Integer>`。

## 提示 3：伪代码

```text
priceFuture = 提交价格任务
stockFuture = 提交库存任务
记录 priceFuture.isDone 和 stockFuture.isDone
price = 等待价格
stock = 等待库存
返回商品摘要
```

关键不是先 `get` 谁，而是必须先把两个任务都提交。
