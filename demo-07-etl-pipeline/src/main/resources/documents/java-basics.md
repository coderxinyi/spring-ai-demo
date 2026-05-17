# Java 基础教程 — 第一章：变量与数据类型

## 1.1 什么是变量

变量是程序中用来存储数据的容器。你可以把变量想象成一个贴了标签的盒子，标签是变量名，盒子里装的就是数据。

在 Java 中，声明一个变量需要指定它的类型和名称：

```java
int age = 15;
String name = "小明";
double score = 98.5;
```

## 1.2 基本数据类型

Java 有 8 种基本数据类型：

- **整型**：`byte`、`short`、`int`、`long`
- **浮点型**：`float`、`double`
- **字符型**：`char`
- **布尔型**：`boolean`

其中 `int` 是最常用的整数类型，`double` 是最常用的小数类型。

```java
int studentCount = 45;        // 班级人数
double averageScore = 87.3;   // 平均分
boolean isPassed = true;      // 是否及格
char grade = 'A';             // 等级
```

## 1.3 变量的命名规则

1. 变量名可以包含字母、数字、下划线和美元符号
2. 变量名不能以数字开头
3. 变量名不能使用 Java 关键字（如 `class`、`int`、`public`）
4. 变量名区分大小写（`age` 和 `Age` 是两个不同的变量）

---

## 练习题

1. 声明一个变量存储你的年龄，并打印出来
2. 声明一个变量存储你的姓名，并打印出来
3. 思考：为什么 `1stPlace` 不是一个合法的变量名？
