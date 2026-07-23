# Java Lambda Lab

这个模块用商品筛选和展示场景练习 Java 8 Lambda。题目代码只包含背景知识、API 参数说明和 TODO；参考答案独立放在 `answers` 包。

学习前建议先查看 [教程索引](docs/guides/README.md)。API 参数说明和代码例子
已经集中到该目录，练习类只保留题目、重点和 TODO。

## 开始

```bash
cd /Users/roy/Documents/代码学习/java-practice-lab/lambda-lab
mvn package
java -cp target/classes com.roy.lambdalab.LambdaLearningConsole 1
```

完成第 1 课后验收：

```bash
mvn package
java -cp target/classes com.roy.lambdalab.LambdaExerciseCheck 1
```

验证标准答案：

```bash
java -cp target/classes com.roy.lambdalab.LambdaSelfCheck
```

## 学习顺序

| 课次 | 主题 | 核心内容 |
| --- | --- | --- |
| 1 | Lambda 基本语法 | 参数列表、箭头、表达式体、代码块、变量捕获 |
| 2 | 内置函数式接口 | `Predicate`、`Function` |
| 3 | 方法引用 | 静态、对象实例、类型实例、构造器引用 |
| 4 | Stream 实战 | `filter`、`sorted`、`map`、`collect` |

第 4 课只是用 Stream 综合练习 Lambda。完成后可进入
[stream-lab](../stream-lab/README.md)，系统学习流式编程。

先看 [学习地图](docs/LEARNING_MAP.md)。卡住时按编号查看 `docs/hints`，最后才打开 `answers`。
