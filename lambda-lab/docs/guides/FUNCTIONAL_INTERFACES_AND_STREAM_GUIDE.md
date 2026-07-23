# 函数式接口与 Stream API 指南

这份文档对应 Lambda 第 2 至第 4 课。Lambda 基础语法、匿名类对照和变量捕获请先看
[Lambda 表达式对照指南](LAMBDA_EXPRESSION_GUIDE.md)。

## Lab 对照表

| Lab | Exercise | 本文知识点 |
| --- | --- | --- |
| Lab 02 | [LambdaLab02FunctionalInterfaces.java](../../src/main/java/com/roy/lambdalab/exercises/LambdaLab02FunctionalInterfaces.java) | Predicate、Function、接口组合 |
| Lab 03 | [LambdaLab03MethodReferences.java](../../src/main/java/com/roy/lambdalab/exercises/LambdaLab03MethodReferences.java) | Consumer、Supplier、方法引用、Comparator |
| Lab 04 | [LambdaLab04Streams.java](../../src/main/java/com/roy/lambdalab/exercises/LambdaLab04Streams.java) | stream、filter、sorted、map、collect |

---

## Lab 02｜Predicate：判断条件

> 对应练习：LambdaLab02FunctionalInterfaces

Predicate 表示“接收一个值，返回 true 或 false”，抽象方法是：

```java
boolean test(T value);
```

- `value`：要检查的对象。
- 返回值：`true` 表示满足条件，`false` 表示不满足。
- Lambda 形状：`value -> boolean表达式`。

```java
Predicate<Integer> positive =
        number -> number > 0;

boolean firstResult = positive.test(10);
boolean secondResult = positive.test(-2);
// firstResult 为 true，secondResult 为 false
```

### Predicate 组合

```java
Predicate<Integer> lessThan100 =
        number -> number < 100;

Predicate<Integer> positiveAndSmall =
        positive.and(lessThan100);

boolean result = positiveAndSmall.test(50);
// result 为 true
```

- `first.and(second)`：两个条件都为 true。
- `first.or(second)`：至少一个条件为 true。
- `first.negate()`：把条件取反。

---

## Lab 02｜Function：转换数据

> 对应练习：LambdaLab02FunctionalInterfaces

Function 表示“把一种值转换成另一种值”，抽象方法是：

```java
R apply(T value);
```

- `value`：输入值。
- 返回值：转换后的结果。
- Lambda 形状：`value -> newValue`。

```java
Function<String, Integer> length =
        text -> text.length();

int result = length.apply("Lambda");
// result 为 6
```

### Function 组合

```java
Function<Integer, String> addUnit =
        number -> number + " 个字符";

Function<String, String> lengthLabel =
        length.andThen(addUnit);

String label = lengthLabel.apply("Java");
// label 为“4 个字符”
```

- `first.andThen(second)`：先执行 first，再把结果交给 second。
- `first.compose(before)`：先执行 before，再把结果交给 first。

---

## Lab 03｜Consumer：消费一个值

> 对应练习：LambdaLab03MethodReferences

Consumer 接收一个参数但不返回结果，抽象方法是：

```java
void accept(T value);
```

```java
Consumer<String> printer =
        message -> System.out.println("收到：" + message);

printer.accept("Hello");
// 输出：收到：Hello
```

---

## Lab 03｜Supplier：产生一个值

> 对应练习：LambdaLab03MethodReferences

Supplier 没有参数并返回一个值，抽象方法是：

```java
T get();
```

```java
Supplier<List<String>> factory =
        ArrayList::new;

List<String> first = factory.get();
List<String> second = factory.get();
// first 与 second 是不同列表
```

---

## Lab 03｜方法引用

> 对应练习：LambdaLab03MethodReferences

当 Lambda 只是把参数原样交给已有方法时，可以使用方法引用。

### 类名::静态方法

```java
Function<String, Integer> parser =
        Integer::parseInt;
```

等价 Lambda：

```java
text -> Integer.parseInt(text)
```

### 已有对象::实例方法

```java
List<String> messages = new ArrayList<>();
Consumer<String> appender = messages::add;

appender.accept("第一条消息");
```

等价 Lambda：

```java
text -> messages.add(text)
```

### 类名::实例方法

```java
Function<Product, String> extractor =
        Product::getName;
```

等价 Lambda：

```java
product -> product.getName()
```

### 类名::new

```java
Supplier<List<String>> factory =
        ArrayList::new;
```

等价 Lambda：

```java
() -> new ArrayList<>()
```

---

## Lab 03｜Comparator

> 对应练习：LambdaLab03MethodReferences

Comparator 接收两个同类型参数并返回 int：

```java
int compare(T first, T second);
```

- 小于 0：first 排在 second 前面。
- 等于 0：两者在当前规则下相等。
- 大于 0：first 排在 second 后面。

```java
Comparator<String> comparator =
        String::compareToIgnoreCase;

int result =
        comparator.compare("apple", "BANANA");
// result 小于 0
```

---

## Lab 04｜Stream API

> 对应练习：LambdaLab04Streams

Stream 描述一条数据处理流水线。中间操作只描述规则，终止操作触发执行。

### collection.stream()

- 参数：没有参数。
- 返回值：按集合遍历顺序产生元素的 Stream。

```java
List<Integer> numbers =
        Arrays.asList(3, 1, 2);

Stream<Integer> stream =
        numbers.stream();
```

### stream.filter(predicate)

- `predicate`：接收一个元素并返回 boolean。
- `true`：保留元素。
- `false`：丢弃元素。
- 返回值：元素类型不变的新 Stream。

```java
Stream<Integer> evenNumbers =
        numbers.stream()
                .filter(number -> number % 2 == 0);
```

### stream.sorted(comparator)

- `comparator`：决定元素顺序。
- 返回值：排序后的新 Stream。

```java
Stream<Integer> sortedNumbers =
        numbers.stream()
                .sorted(Comparator.naturalOrder());
```

根据对象中的 int 属性排序：

```java
products.stream()
        .sorted(
                Comparator.comparingInt(
                        Product::getPriceCents));
```

### stream.map(mapper)

- `mapper`：把当前元素转换为另一种值。
- 返回值：元素类型可以变化的新 Stream。

```java
Stream<String> texts =
        numbers.stream()
                .map(String::valueOf);
```

### stream.collect(collector)

- `collector`：描述如何汇总元素。
- `Collectors.toList()`：把所有元素收集成 List。
- collect 是终止操作，会触发整条流水线。

```java
List<String> result = numbers.stream()
        .filter(number -> number > 1)
        .sorted()
        .map(String::valueOf)
        .collect(Collectors.toList());

// result 为 ["2", "3"]
```

### 操作顺序

```text
集合
 -> filter：尽早减少元素
 -> sorted：在仍然拥有排序字段时排序
 -> map：转换成目标类型
 -> collect：触发执行并收集结果
```

Stream 不修改原集合，也不能重复消费同一个 Stream 实例。
