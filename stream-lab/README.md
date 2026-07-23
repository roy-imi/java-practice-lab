# Java Stream Lab

这是一个独立的 Java 8 流式编程练习模块。Lambda 是表达处理规则的语法，Stream 是组织数据处理步骤的 API；本模块专门学习后者。

## 开始

先阅读 [Stream 教程索引](docs/guides/README.md)，然后运行第 1 课：

```bash
cd /Users/roy/Documents/代码学习/java-practice-lab/stream-lab
mvn package
java -cp target/classes com.roy.streamlab.StreamLearningConsole 1
```

完成后验收：

```bash
mvn package
java -cp target/classes com.roy.streamlab.StreamExerciseCheck 1
```

验证标准答案：

```bash
java -cp target/classes com.roy.streamlab.StreamSelfCheck
```

## 学习顺序

| Lab | 主题 | 核心 API |
| --- | --- | --- |
| 01 | 基础流水线 | `stream`、`filter`、`map`、`collect` |
| 02 | 排序、去重、截断 | `sorted`、`distinct`、`limit` |
| 03 | 扁平化嵌套集合 | `flatMap` |
| 04 | 终止操作与 Optional | `mapToInt`、`sum`、`anyMatch`、`findFirst` |
| 05 | 分组和分区 | `groupingBy`、`summingInt`、`partitioningBy` |
