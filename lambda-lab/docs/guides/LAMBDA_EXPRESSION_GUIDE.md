# Lambda 表达式对照指南

Lambda 是函数式接口匿名实现的简写。阅读或编写 Lambda 时，先找到函数式接口唯一的抽象方法，再判断它需要几个参数、有没有返回值。

## Lab 对照表

| 知识点 | 对应 Lab |
| --- | --- |
| 参数列表、`->`、表达式体、变量捕获 | Lab 01 |
| Runnable、IntBinaryOperator | Lab 01 |
| Predicate、Function | Lab 02 |
| Supplier、Consumer、Comparator、方法引用 | Lab 03 |

---

## Lab 01｜Lambda 的基本结构

> 对应练习：[LambdaLab01Syntax.java](../../src/main/java/com/roy/lambdalab/exercises/LambdaLab01Syntax.java)

```java
(参数列表) -> 表达式或代码块
```

`->` 左边是 Lambda 的形参，右边是具体实现。

```java
() -> value
x -> x * 2
(a, b) -> a + b
```

- `()`：没有形参。
- `x`：一个形参，一个参数时可以省略圆括号。
- `(a, b)`：两个形参，多个参数必须使用圆括号。

调用函数式接口方法时传入的是实参：

```java
Predicate<Product> predicate =
        product -> product.getStock() > 0;

predicate.test(actualProduct);
```

对应关系：

```text
actualProduct：实参
product：Lambda 形参
```

---

## Runnable：无参数、无返回值

> 对应 Lab：Lab 01

Runnable 的抽象方法：

```java
void run();
```

匿名类写法：

```java
Runnable task = new Runnable() {
    @Override
    public void run() {
        System.out.println("开始执行任务");
    }
};
```

Lambda 写法：

```java
Runnable task =
        () -> System.out.println("开始执行任务");
```

调用方式：

```java
task.run();
```

对应关系：

```text
void run()
     ↓
() -> 执行代码
```

这里的 `()` 表示 `run()` 没有参数。

---

## Supplier：无参数、有返回值

> 对应 Lab：Lab 03

Supplier 的抽象方法：

```java
T get();
```

匿名类写法：

```java
Supplier<Integer> priceSupplier =
        new Supplier<Integer>() {
            @Override
            public Integer get() {
                return 19900;
            }
        };
```

Lambda 写法：

```java
Supplier<Integer> priceSupplier =
        () -> 19900;
```

调用方式：

```java
Integer price = priceSupplier.get();
```

因为右边只有一个表达式，所以可以省略大括号和 `return`。

如果有多条语句，需要使用代码块并显式返回：

```java
Supplier<Integer> priceSupplier = () -> {
    System.out.println("正在查询价格");
    int price = 19900;
    return price;
};
```

---

## Consumer：一个参数、无返回值

> 对应 Lab：Lab 01、Lab 03

Consumer 的抽象方法：

```java
void accept(T value);
```

匿名类写法：

```java
Consumer<String> printer =
        new Consumer<String>() {
            @Override
            public void accept(String message) {
                System.out.println(message);
            }
        };
```

Lambda 写法：

```java
Consumer<String> printer =
        message -> System.out.println(message);
```

调用方式：

```java
printer.accept("Hello Lambda");
```

`message` 是形参，调用 `accept()` 时传入的字符串是实参。

---

## Predicate：一个参数、返回 boolean

> 对应 Lab：Lab 02

Predicate 的抽象方法：

```java
boolean test(T value);
```

匿名类写法：

```java
Predicate<Product> inStock =
        new Predicate<Product>() {
            @Override
            public boolean test(Product product) {
                return product.getStock() > 0;
            }
        };
```

Lambda 写法：

```java
Predicate<Product> inStock =
        product -> product.getStock() > 0;
```

调用方式：

```java
boolean result = inStock.test(product);
```

右边表达式的结果是 boolean，因此它会自动成为 Lambda 的返回值。

参数类型也可以显式写出：

```java
Predicate<Product> inStock =
        (Product product) -> product.getStock() > 0;
```

通常编译器可以根据 `Predicate<Product>` 推断参数类型，所以能够省略。

---

## Function：一个参数、返回转换结果

> 对应 Lab：Lab 02、Lab 03

Function 的抽象方法：

```java
R apply(T value);
```

匿名类写法：

```java
Function<Product, String> nameExtractor =
        new Function<Product, String>() {
            @Override
            public String apply(Product product) {
                return product.getName();
            }
        };
```

Lambda 写法：

```java
Function<Product, String> nameExtractor =
        product -> product.getName();
```

方法引用写法：

```java
Function<Product, String> nameExtractor =
        Product::getName;
```

调用方式：

```java
String name = nameExtractor.apply(product);
```

三种写法表达的是同一个功能：

```java
// 匿名类
new Function<Product, String>() {
    @Override
    public String apply(Product product) {
        return product.getName();
    }
};

// Lambda
product -> product.getName();

// 方法引用
Product::getName;
```

---

## IntBinaryOperator：两个 int 参数、返回 int

> 对应 Lab：Lab 01

IntBinaryOperator 的抽象方法：

```java
int applyAsInt(int left, int right);
```

匿名类写法：

```java
IntBinaryOperator discountCalculator =
        new IntBinaryOperator() {
            @Override
            public int applyAsInt(int price, int discount) {
                return price * (100 - discount) / 100;
            }
        };
```

Lambda 写法：

```java
IntBinaryOperator discountCalculator =
        (price, discount) ->
                price * (100 - discount) / 100;
```

调用方式：

```java
int result =
        discountCalculator.applyAsInt(19900, 10);
```

参数对应关系：

```text
price = 19900
discount = 10
result = 17910
```

两个及以上参数必须使用圆括号。

---

## Comparator：两个参数、返回比较结果

> 对应 Lab：Lab 03

Comparator 的抽象方法：

```java
int compare(T first, T second);
```

匿名类写法：

```java
Comparator<String> comparator =
        new Comparator<String>() {
            @Override
            public int compare(String first, String second) {
                return first.compareToIgnoreCase(second);
            }
        };
```

Lambda 写法：

```java
Comparator<String> comparator =
        (first, second) ->
                first.compareToIgnoreCase(second);
```

方法引用写法：

```java
Comparator<String> comparator =
        String::compareToIgnoreCase;
```

调用方式：

```java
int result = comparator.compare("hello", "WORLD");
```

Comparator 返回值含义：

- 小于 0：第一个值排在第二个值前面。
- 等于 0：两个值在当前排序规则下相等。
- 大于 0：第一个值排在第二个值后面。

---

## Lambda 捕获外部变量

> 对应 Lab：Lab 01

```java
String prefix = "商品：";

Function<Product, String> labelFunction =
        product -> prefix + product.getName();
```

大致对应：

```java
String prefix = "商品：";

Function<Product, String> labelFunction =
        new Function<Product, String>() {
            @Override
            public String apply(Product product) {
                return prefix + product.getName();
            }
        };
```

这里有两类变量：

```text
product：Lambda 形参
prefix：从外部捕获的变量
```

被捕获的局部变量必须是 final，或者事实上没有被重新赋值：

```java
String prefix = "商品：";

Function<Product, String> function =
        product -> prefix + product.getName();

// 这里不能再写 prefix = "产品：";
```

---

## 常用函数式接口速查

> 对应 Lab：Lab 01 至 Lab 03

| 函数式接口 | 抽象方法 | Lambda 形状 | 用途 |
| --- | --- | --- | --- |
| `Runnable` | `void run()` | `() -> 执行操作` | 无参数任务 |
| `Supplier<T>` | `T get()` | `() -> value` | 产生一个值 |
| `Consumer<T>` | `void accept(T value)` | `value -> 执行操作` | 消费一个值 |
| `Predicate<T>` | `boolean test(T value)` | `value -> boolean` | 判断条件 |
| `Function<T, R>` | `R apply(T value)` | `value -> newValue` | 转换数据 |
| `IntBinaryOperator` | `int applyAsInt(int a, int b)` | `(a, b) -> int` | 两个 int 的运算 |
| `Comparator<T>` | `int compare(T a, T b)` | `(a, b) -> int` | 比较与排序 |

---

## 编写 Lambda 的思考顺序

> 对应 Lab：所有 Lambda 练习

```text
1. 找到函数式接口唯一的抽象方法
                ↓
2. 方法有几个参数？
                ↓
3. 把形参写在 -> 左边
                ↓
4. 方法是否需要返回值？
                ↓
5. 在 -> 右边执行操作或计算结果
                ↓
6. 如果只是调用一个已有方法，考虑改成方法引用
```
