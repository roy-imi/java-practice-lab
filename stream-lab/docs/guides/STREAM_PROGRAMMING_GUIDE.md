# Java Stream 流式编程指南

Stream 是 Java 8 提供的数据处理 API。它不保存数据，而是描述数据应该经过哪些处理步骤。

## Lab 对照表

| Lab | Exercise | 本文知识点 |
| --- | --- | --- |
| Lab 01 | [StreamLab01Pipeline.java](../../src/main/java/com/roy/streamlab/exercises/StreamLab01Pipeline.java) | stream、filter、map、collect |
| Lab 02 | [StreamLab02SortDistinctLimit.java](../../src/main/java/com/roy/streamlab/exercises/StreamLab02SortDistinctLimit.java) | sorted、distinct、limit、操作顺序 |
| Lab 03 | [StreamLab03FlatMap.java](../../src/main/java/com/roy/streamlab/exercises/StreamLab03FlatMap.java) | map 与 flatMap |
| Lab 04 | [StreamLab04TerminalOperations.java](../../src/main/java/com/roy/streamlab/exercises/StreamLab04TerminalOperations.java) | mapToInt、sum、匹配、findFirst、Optional |
| Lab 05 | [StreamLab05Collectors.java](../../src/main/java/com/roy/streamlab/exercises/StreamLab05Collectors.java) | groupingBy、summingInt、partitioningBy |

---

## Lab 01｜建立流式编程思维

> 对应练习：StreamLab01Pipeline

### Stream 不是集合

集合负责保存数据，Stream 负责描述如何处理数据。

```text
List：仓库里的商品
Stream：商品经过的加工流水线
```

Stream 不会自动修改原集合。流水线结束后，原集合通常保持不变。

### 外部迭代与内部迭代

传统循环由程序员控制每一步：

```java
List<String> result = new ArrayList<>();

for (Integer number : numbers) {
    if (number % 2 == 0) {
        result.add(String.valueOf(number * 10));
    }
}
```

Stream 写法描述“做什么”：

```java
List<String> result = numbers.stream()
        .filter(number -> number % 2 == 0)
        .map(number -> number * 10)
        .map(String::valueOf)
        .collect(Collectors.toList());
```

对应关系：

```text
for 遍历                 -> stream()
if 判断                  -> filter()
把元素转换成另一个值     -> map()
把结果添加到新列表       -> collect(toList())
```

### collection.stream()

- 参数：没有参数。
- 返回值：按集合遍历顺序产生元素的 Stream。
- 此时只是创建流水线来源，通常还没有处理元素。

```java
List<Integer> numbers =
        Arrays.asList(1, 2, 3, 4);

Stream<Integer> stream =
        numbers.stream();
```

### stream.filter(predicate)

- `predicate`：接收一个元素并返回 boolean 的规则。
- 返回 `true`：保留元素。
- 返回 `false`：丢弃元素。
- 返回值：元素类型不变的新 Stream。

```java
Stream<Integer> evenNumbers =
        numbers.stream()
                .filter(number -> number % 2 == 0);
```

元素类型变化：

```text
filter 前：Stream<Integer>
filter 后：Stream<Integer>
```

### stream.map(mapper)

- `mapper`：接收一个元素并返回转换结果的 Function。
- 返回值：包含转换结果的新 Stream。
- 元素类型可以改变。

```java
Stream<String> numberTexts =
        numbers.stream()
                .map(String::valueOf);
```

元素类型变化：

```text
map 前：Stream<Integer>
map 后：Stream<String>
```

### stream.collect(collector)

- `collector`：描述怎样汇总元素。
- `Collectors.toList()`：收集成 List。
- `collect` 是终止操作，会触发整条流水线。

```java
List<String> texts = numbers.stream()
        .filter(number -> number > 1)
        .map(String::valueOf)
        .collect(Collectors.toList());

// texts 为 ["2", "3", "4"]
```

### 惰性执行

`filter` 和 `map` 是中间操作。只有中间操作时，元素通常不会被处理：

```java
Stream<Integer> pipeline = numbers.stream()
        .filter(number -> {
            System.out.println("检查 " + number);
            return number > 1;
        });

// 到这里通常没有输出
```

增加终止操作后才执行：

```java
long count = pipeline.count();
```

### 一条 Stream 只能消费一次

```java
Stream<Integer> stream = numbers.stream();

long count = stream.count();
// stream.forEach(System.out::println);
// 错误：stream 已经被终止操作消费
```

需要再次处理时，应从数据源创建新的 Stream。

---

## Lab 02｜排序、去重和截断

> 对应练习：StreamLab02SortDistinctLimit

### stream.sorted(comparator)

- `comparator`：接收两个元素并决定顺序。
- 返回值：排序后的新 Stream。
- sorted 是有状态中间操作，需要观察多个元素后才能确定顺序。

自然顺序：

```java
List<Integer> result = numbers.stream()
        .sorted()
        .collect(Collectors.toList());
```

按照对象属性排序：

```java
List<Person> result = people.stream()
        .sorted(
                Comparator.comparingInt(
                        Person::getAge))
        .collect(Collectors.toList());
```

从大到小：

```java
Comparator<Person> byAgeDescending =
        Comparator.comparingInt(Person::getAge)
                .reversed();
```

多个排序条件：

```java
Comparator<Person> comparator =
        Comparator.comparingInt(Person::getAge)
                .thenComparing(Person::getName);
```

### stream.distinct()

- 参数：没有参数。
- 返回值：去除重复元素的新 Stream。
- 判断重复依赖元素的 `equals()` 和 `hashCode()`。
- 对有顺序的 Stream，保留第一次出现的元素。

```java
List<String> values =
        Arrays.asList("A", "B", "A", "C");

List<String> result = values.stream()
        .distinct()
        .collect(Collectors.toList());

// result 为 ["A", "B", "C"]
```

对自定义对象使用 distinct 时，应确认对象是否正确实现 `equals()` 和 `hashCode()`。

### stream.limit(maxSize)

- `maxSize`：最多保留多少个元素，类型为 long。
- 返回值：不超过指定数量的新 Stream。
- 遇到有顺序的 Stream 时，保留前面的元素。

```java
List<Integer> firstThree = numbers.stream()
        .limit(3)
        .collect(Collectors.toList());
```

### stream.skip(count)

- `count`：跳过前多少个元素。
- 返回值：去掉开头指定数量后的新 Stream。

```java
List<Integer> afterFirstTwo = numbers.stream()
        .skip(2)
        .collect(Collectors.toList());
```

### 操作顺序会改变结果

```java
values.stream()
        .distinct()
        .limit(2);
```

表示先去重，再取两个不同值。

```java
values.stream()
        .limit(2)
        .distinct();
```

表示先取前两个元素，再在这两个元素中去重。

排序后取前几项：

```java
people.stream()
        .sorted(byScoreDescending)
        .limit(3);
```

这代表“分数最高的前三名”。如果先 limit 再 sorted，只会排序原列表前面的三个人。

### 为什么经常先 filter

```java
orders.stream()
        .filter(Order::isValid)
        .sorted(byAmount);
```

先过滤可以减少需要参与排序的元素数量，通常更高效。

---

## Lab 03｜map 与 flatMap

> 对应练习：StreamLab03FlatMap

### map 是一对一转换

每个输入元素产生一个输出元素：

```java
List<String> names = people.stream()
        .map(Person::getName)
        .collect(Collectors.toList());
```

```text
Person -> String
```

如果每个人都有多个电话号码：

```java
Stream<List<String>> nested =
        people.stream()
                .map(Person::getPhoneNumbers);
```

map 后仍然是嵌套结构：

```text
Stream<Person>
    -> Stream<List<String>>
```

### flatMap 是一对多后再铺平

`flatMap` 的 mapper 必须返回一个 Stream。它会把多个子 Stream 连接成一个 Stream。

```java
Stream<String> phoneNumbers =
        people.stream()
                .flatMap(
                        person ->
                                person.getPhoneNumbers().stream());
```

结构变化：

```text
Stream<Person>
    -> 每个 Person 产生 Stream<String>
    -> 合并成一个 Stream<String>
```

### 句子拆单词示例

```java
List<String> sentences =
        Arrays.asList(
                "java stream",
                "lambda stream");

List<String> words = sentences.stream()
        .flatMap(sentence ->
                Arrays.stream(sentence.split(" ")))
        .distinct()
        .collect(Collectors.toList());

// words 为 ["java", "stream", "lambda"]
```

如果使用 map：

```java
sentences.stream()
        .map(sentence ->
                Arrays.stream(sentence.split(" ")));
```

结果会是 `Stream<Stream<String>>`，仍有两层。

### 选择方法

```text
一个输入 -> 一个普通值          map
一个输入 -> 多个值的 Stream     flatMap
```

---

## Lab 04｜终止操作、数值流与 Optional

> 对应练习：StreamLab04TerminalOperations

下面涉及人员的 API 示例统一使用这组数据：

```text
P-01，小明，18 岁
P-02，小红，25 岁
P-03，小刚，17 岁
P-04，小丽，20 岁
```

涉及数字的示例统一使用：

```java
List<Integer> numbers =
        Arrays.asList(10, -3, 20, 0);
```

### mapToInt(mapper)

- `mapper`：接收元素并返回 int 的 `ToIntFunction`。
- 返回值：`IntStream`。
- IntStream 提供 `sum()`、`average()`、`max()` 等数值操作。
- 避免 Integer 装箱和拆箱。

```java
int totalAge = people.stream()
        .mapToInt(Person::getAge)
        .sum();
```

类型变化：

```text
Stream<Person>
    -> IntStream
    -> int
```

结果：

```text
totalAge = 80
```

### sum()

- 参数：没有参数。
- 返回值：所有 int 元素的总和。
- 空 IntStream 的和是 0。

```java
int total = numbers.stream()
        .mapToInt(Integer::intValue)
        .sum();
```

结果：

```text
total = 27
```

空流示例：

```java
int emptyTotal = IntStream.empty().sum();
```

结果：

```text
emptyTotal = 0
```

### average() 与 max()

两者都没有参数。因为空流没有平均值或最大值，所以不会直接返回基本类型：

- `IntStream.average()` 返回 `OptionalDouble`。
- `IntStream.max()` 返回 `OptionalInt`。

```java
OptionalDouble averageAge = people.stream()
        .mapToInt(Person::getAge)
        .average();

OptionalInt maxAge = people.stream()
        .mapToInt(Person::getAge)
        .max();
```

结果：

```text
averageAge = OptionalDouble[20.0]
maxAge = OptionalInt[25]
```

### count()

- 参数：没有参数。
- 返回值：元素数量，类型为 long。

```java
long adultCount = people.stream()
        .filter(person -> person.getAge() >= 18)
        .count();
```

结果：

```text
adultCount = 3
```

### anyMatch、allMatch、noneMatch

三者都接收一个 `Predicate`：

- `predicate`：接收一个元素并返回 boolean 的判断规则。
- `anyMatch`：至少一个元素满足条件时返回 true。
- `allMatch`：所有元素都满足条件时返回 true。
- `noneMatch`：没有元素满足条件时返回 true。

```java
boolean hasNegative =
        numbers.stream()
                .anyMatch(number -> number < 0);

boolean allPositive =
        numbers.stream()
                .allMatch(number -> number > 0);

boolean noZero =
        numbers.stream()
                .noneMatch(number -> number == 0);
```

结果：

```text
hasNegative = true
allPositive = false
noZero = false
```

短路表示一旦能确定结果，就不再检查后续元素。

### findFirst()

- 参数：没有参数。
- 返回值：包含第一个元素的 `Optional<T>`。
- 有顺序的 Stream 返回第一个匹配元素。
- 没有元素时返回空 Optional，而不是返回 null。

```java
Optional<Person> firstAdult = people.stream()
        .filter(person -> person.getAge() >= 18)
        .findFirst();
```

结果：

```text
firstAdult = Optional[小明（18 岁）]
```

找不到元素时：

```java
Optional<Person> retired = people.stream()
        .filter(person -> person.getAge() >= 60)
        .findFirst();
```

结果：

```text
retired = Optional.empty
```

### Optional

`Optional.get()` 没有参数，有值时返回内部值；为空时抛出
`NoSuchElementException`，因此不应在不确定是否有值时直接调用：

```java
String value = optional.get();
```

`orElse(other)`：

- `other`：Optional 为空时使用的默认值。
- 返回值：内部值或默认值。

示例中先使用了 `Optional.map(mapper)`：

- `mapper`：有值时把内部值转换成另一种类型。
- 返回值：包含转换结果的新 Optional；原 Optional 为空时仍为空。

```java
String adultName = firstAdult
        .map(Person::getName)
        .orElse("未找到");
```

结果：

```text
adultName = 小明
```

`ifPresent(consumer)`：

- `consumer`：有值时负责消费该值的 Consumer。
- Optional 为空时不执行任何操作。

```java
firstAdult.ifPresent(
        person -> System.out.println(
                person.getName()));
```

输出：

```text
小明
```

`orElseGet(supplier)`：

- `supplier`：Optional 为空时才调用，用于延迟创建默认值。
- 返回值：内部值或 Supplier 创建的值。

```java
String retiredName = retired
        .map(Person::getName)
        .orElseGet(() -> "暂无退休人员");
```

结果：

```text
retiredName = 暂无退休人员
```

`orElse` 的参数会提前计算；`orElseGet` 的 Supplier 只在 Optional
为空时执行。

### reduce()

reduce 把多个元素逐步合成一个结果。

带初始值：

```java
int total = numbers.stream()
        .reduce(
                0,
                (sum, number) -> sum + number);
```

- `identity`：初始值，本例是 0。
- `accumulator`：接收当前累计值和下一个元素，返回新累计值。
- 返回值：最终累计结果。

计算过程：

```text
0 + 10 = 10
10 + (-3) = 7
7 + 20 = 27
27 + 0 = 27
```

最终结果：

```text
total = 27
```

没有初始值时返回 Optional：

```java
Optional<Integer> max = numbers.stream()
        .reduce(Integer::max);
```

结果：

```text
max = Optional[20]
```

空 Stream 使用无初始值的 `reduce` 时，结果是 `Optional.empty`。

简单数值求和优先使用 `mapToInt().sum()`，语义更直接。

---

## Lab 05｜Collectors 分组与分区

> 对应练习：StreamLab05Collectors

下面的 API 示例统一使用这组人员数据：

```text
P-01，小明，上海，18 岁
P-02，小红，北京，25 岁
P-03，小刚，上海，20 岁
P-04，小红，深圳，17 岁
```

### Collectors.groupingBy(classifier)

- `classifier`：接收元素并返回分组键。
- 返回值：以分组键为 key、元素列表为 value 的 Map。

```java
Map<String, List<Person>> peopleByCity =
        people.stream()
                .collect(
                        Collectors.groupingBy(
                                Person::getCity));
```

结构：

```text
上海 -> [小明（18 岁）, 小刚（20 岁）]
北京 -> [小红（25 岁）]
深圳 -> [小红（17 岁）]
```

### 下游 Collector

`groupingBy` 的第二个参数定义每组内部如何汇总。

每个城市的人数：

```java
Map<String, Long> countByCity =
        people.stream()
                .collect(
                        Collectors.groupingBy(
                                Person::getCity,
                                Collectors.counting()));
```

结果：

```text
上海 -> 2
北京 -> 1
深圳 -> 1
```

每个城市的年龄总和：

```java
Map<String, Integer> ageByCity =
        people.stream()
                .collect(
                        Collectors.groupingBy(
                                Person::getCity,
                                Collectors.summingInt(
                                        Person::getAge)));
```

### Collectors.summingInt(mapper)

- `mapper`：把元素映射为 int。
- 返回值：把 int 值求和的 Collector。
- 经常作为 groupingBy 的下游 Collector。

```java
Collectors.summingInt(Person::getAge)
```

结合 `groupingBy(Person::getCity, ...)` 后，结果是：

```text
上海 -> 38
北京 -> 25
深圳 -> 17
```

对应的 Map 可以表示为：

```java
{上海=38, 北京=25, 深圳=17}
```

### Collectors.partitioningBy(predicate)

partitioningBy 只分成两组，Map key 固定为 boolean：

- `true`：满足条件。
- `false`：不满足条件。

```java
Map<Boolean, List<Person>> adults =
        people.stream()
                .collect(
                        Collectors.partitioningBy(
                                person -> person.getAge() >= 18));
```

结果：

```text
true  -> [小明（18 岁）, 小红（25 岁）, 小刚（20 岁）]
false -> [小红（17 岁）]
```

`groupingBy` 可以有任意多个分组键，`partitioningBy` 只有 true 和 false 两组。

### Collectors.toMap(keyMapper, valueMapper)

- `keyMapper`：从元素中取得 Map 的 key。
- `valueMapper`：从元素中取得 Map 的 value。
- 返回值：由 Stream 元素组成的 Map。

```java
Map<String, String> nameById =
        people.stream()
                .collect(
                        Collectors.toMap(
                                Person::getId,
                                Person::getName));
```

结果：

```java
{P-01=小明, P-02=小红, P-03=小刚, P-04=小红}
```

如果 key 重复，默认会抛异常。可以提供合并函数：

```java
Map<String, Person> personByName =
        people.stream()
                .collect(
                        Collectors.toMap(
                                Person::getName,
                                person -> person,
                                (first, second) -> first));
```

- 第三个参数 `mergeFunction`：key 重复时接收旧值和新值，并决定保留哪个值。
- 上面的 `(first, second) -> first` 表示保留第一次出现的人员。

结果中“小红”只保留 `P-02`：

```text
小明 -> P-01
小红 -> P-02
小刚 -> P-03
```

### Collectors.joining(delimiter)

- `delimiter`：每两个字符串之间的分隔符。
- 返回值：连接完成的 String。

```java
String names = people.stream()
        .map(Person::getName)
        .collect(Collectors.joining(", "));
```

结果：

```text
小明, 小红, 小刚, 小红
```

`joining` 还有一个三参数重载：

```java
String names = people.stream()
        .map(Person::getName)
        .collect(Collectors.joining(
                ", ",
                "[",
                "]"));
```

- 第一个参数：元素之间的分隔符。
- 第二个参数：整个结果的前缀。
- 第三个参数：整个结果的后缀。

结果：

```text
[小明, 小红, 小刚, 小红]
```

Map 的默认实现通常不保证打印顺序，因此示例中的键可能以不同顺序显示；判断结果时应关注键和值的对应关系。

---

## 通用知识｜副作用、调试和并行流

> 对应 Lab：Lab 01 至 Lab 05

### 避免副作用

不推荐：

```java
List<String> result = new ArrayList<>();

people.stream()
        .map(Person::getName)
        .forEach(result::add);
```

推荐：

```java
List<String> result = people.stream()
        .map(Person::getName)
        .collect(Collectors.toList());
```

依赖外部可变状态会让代码更难测试，也可能在并行流中产生线程安全问题。

### peek 主要用于调试

```java
List<String> result = numbers.stream()
        .peek(number ->
                System.out.println("原始：" + number))
        .filter(number -> number > 1)
        .peek(number ->
                System.out.println("过滤后：" + number))
        .map(String::valueOf)
        .collect(Collectors.toList());
```

不要把重要业务副作用放进 peek；惰性执行和短路可能让它不按预期执行。

### null

Stream 不会自动跳过 null：

```java
values.stream()
        .filter(Objects::nonNull)
        .map(String::trim);
```

### parallelStream

```java
list.parallelStream()
```

并行流不保证更快。它有任务拆分、线程调度和结果合并成本，并使用公共 ForkJoinPool。

适合考虑并行的场景通常是：

- 数据量足够大。
- 每个元素计算成本较高。
- 操作容易拆分。
- 没有共享可变状态。
- 已经过基准测试证明更快。

数据库查询、网络请求等阻塞 I/O 不应仅因为想“并发”就直接改成 parallelStream。

### 常见错误检查表

1. 同一个 Stream 是否被终止操作消费了两次？
2. `map` 后的元素类型是否符合下一步要求？
3. 是否应该使用 `flatMap` 展开嵌套结构？
4. `distinct` 的对象是否正确实现 equals 和 hashCode？
5. `limit` 与 `sorted` 的顺序是否表达正确业务含义？
6. Optional 是否可能为空？
7. Lambda 中是否修改了外部集合或计数器？
8. Collector 遇到重复 key 时会怎样处理？
