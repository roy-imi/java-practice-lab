# 第 2 课提示

## 提示 1：Predicate 的结果

Lambda 参数是一件商品。把“库存大于 0”和“价格不超过预算”两个 boolean 表达式用逻辑与连接。

## 提示 2：Function 的输入输出

输入类型是 `Product`，输出类型是 `String`。先单独写出如何从商品得到目标字符串，再把商品变量放到箭头左边。

## 提示 3：金额工具

导入 `com.roy.lambdalab.common.Money`，使用 `Money.format(product.getPriceCents())`，无需自己处理小数点。
