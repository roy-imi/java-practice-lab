# Lambda 学习地图

## Lambda 是什么

Lambda 是“函数式接口实例”的简写。它不是一段可以脱离类型独立存在的代码；编译器会根据上下文判断它要实现哪个函数式接口。

函数式接口只有一个抽象方法。例如：

- `Runnable`：无参数、无返回值。
- `Supplier`：无参数、有返回值。
- `Consumer`：接收一个参数、无返回值。
- `Predicate`：接收一个参数、返回 boolean。
- `Function`：接收一个参数、返回转换后的结果。
- `IntBinaryOperator`：接收两个 int，返回一个 int。

## 语法形状

```text
() -> value                 无参数，返回一个值
x -> x * 2                  一个参数，表达式结果自动返回
(a, b) -> a + b             两个参数
x -> {                      多条语句需要大括号
    int result = x * 2;
    return result;
}
```

参数类型通常可以由编译器推断。一个参数时可以省略圆括号；零个或多个参数时必须保留圆括号。

## 目标类型

相同的 Lambda 外形可能匹配不同接口：

```text
() -> doSomething()
```

它既可能是 `Runnable`，也可能是 `Supplier`。区别取决于 `doSomething()` 是否产生返回值，以及接收 Lambda 的变量或方法参数要求什么类型。

## 变量捕获

Lambda 可以读取外层局部变量，但该变量必须是 final 或“事实上不再修改”的 effectively final。Lambda 保存的是所需上下文，而不是给外层局部变量提供随意修改的通道。

## 推荐顺序

1. 先根据函数式接口的抽象方法确定参数数量和返回值。
2. 再写 Lambda 参数列表。
3. 最后写表达式体。
4. Lambda 只是简单转发已有方法时，考虑替换为方法引用。
5. 熟悉 Predicate 和 Function 后，再进入 Stream。
