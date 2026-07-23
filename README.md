# Java Practice Lab

这是一个面向 Java 8 的多主题练习项目。每个主题都是独立 Maven 模块，可以单独学习、编译和验收。

```text
java-practice-lab/
├── future-lab/   Future 与 CompletableFuture
└── lambda-lab/   Lambda、函数式接口、方法引用与 Stream
```

每个模块都采用相同的学习结构：

- `exercises`：练习代码，只保留课程简介、练习重点和 TODO。
- `answers`：独立参考答案，完成练习后再打开。
- `docs/guides`：集中存放背景知识、API 参数解析和代码例子。
- `docs/hints`：逐级提示。
- `LearningConsole`：观察练习运行效果。
- `ExerciseCheck`：只验收练习代码，不调用答案。
- `SelfCheck`：验证参考答案和模块基础设施。

## 选择学习主题

进入 Lambda 模块：

```bash
cd /Users/roy/Documents/代码学习/java-practice-lab/lambda-lab
mvn package
java -cp target/classes com.roy.lambdalab.LambdaLearningConsole 1
```

进入 Future 模块：

```bash
cd /Users/roy/Documents/代码学习/java-practice-lab/future-lab
mvn package
java -cp target/classes com.roy.futurelab.LearningConsole 1
```

也可以在项目根目录只构建一个模块：

```bash
mvn -pl lambda-lab package
mvn -pl future-lab package
```

建议先学习 Lambda，再继续 CompletableFuture。后者大量使用 Supplier、Function、Consumer 和 BiFunction；理解 Lambda 后，异步流水线会直观很多。
