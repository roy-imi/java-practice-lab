# 第 4 课提示

## 提示 1：一段一段写

先只完成 `stream()` 和 `filter()`，确认 Lambda 返回 boolean；再继续增加排序、转换和收集。

## 提示 2：排序键

`Comparator.comparingInt(...)` 需要一个“输入商品、输出 int”的函数。商品价格 getter 正好符合。

## 提示 3：类型变化

`filter` 和 `sorted` 之后，Stream 元素仍是 `Product`；经过 `map` 提取名称后，元素才变为 `String`。最后使用 `Collectors.toList()`。
