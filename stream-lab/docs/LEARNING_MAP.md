# Stream 学习地图

## Stream 与 Lambda 的关系

Stream 是数据处理 API，Lambda 是向 Stream 提供处理规则的常用语法。

```text
Stream：组织数据处理步骤
Lambda：描述每一步怎么处理元素
```

两者可以独立使用，但 Java Stream 配合 Lambda 最简洁。

## 一条 Stream 流水线

```text
数据源
  -> 中间操作
  -> 中间操作
  -> 终止操作
  -> 最终结果
```

- 数据源：集合、数组、文件行、生成器等。
- 中间操作：`filter`、`map`、`sorted`、`distinct`、`flatMap`。
- 终止操作：`collect`、`sum`、`count`、`findFirst`、`reduce`。

中间操作通常是惰性的。没有终止操作，流水线通常不会真正处理元素。

## 推荐顺序

1. Lab 01：理解来源、中间操作和终止操作。
2. Lab 02：理解有状态操作及操作顺序。
3. Lab 03：掌握一对多转换和扁平化。
4. Lab 04：取得单值结果并处理可能不存在的值。
5. Lab 05：把数据汇总成 Map 和分组结果。

## 每课自问

1. 当前 Stream 中的元素类型是什么？
2. 这一操作是中间操作还是终止操作？
3. 操作是否改变元素类型？
4. 是否依赖元素顺序？
5. 是否引入了副作用？
