# Spring TayBct Tools

[![GitHub license](https://img.shields.io/github/license/taybct/spring-taybct-tools?style=flat)](./LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/taybct/spring-taybct-tools?color=fa6470&style=flat)](https://github.com/taybct/spring-taybct-tools/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/taybct/spring-taybct-tools?style=flat)](https://github.com/taybct/spring-taybct-tools/network/members)

#### 介绍

Spring TayBct Tools 是一个Spring 业务组件基础集成的工具类库，对一些常用的中间件做了基础的常用的集成，并且提供一些业务开发过程中常用的功能模块集成，开箱即用。

#### 软件架构

- 基于 spring boot 开发，版本和 spring boot 大版本基本同步，例如 spring-taybct 3.5.x -> spring-boot 3.5.x.
- 适配一些 spring 相关的基础组件的简单基础集成

#### 安装教程

maven 引入依赖

```xml
<dependencyManagement>
   <dependencies>
      <!--工具类依赖版本管理-->
      <dependency>
         <groupId>io.github.taybct</groupId>
         <artifactId>spring-taybct-tools-dependencies</artifactId>
         <version>${project.version}</version>
         <type>pom</type>
         <scope>import</scope>
      </dependency>
   </dependencies>
</dependencyManagement>
```

#### 使用说明

1. 只是工具类不是运行的代码
2. 没有业务的代码，全是工具类
3. 不引入对应的需要的依赖一些功能就是无效的

#### 参与贡献

1. Fork 本仓库
2. 新建 Feat_xxx 分支
3. 提交代码
4. 新建 Pull Request
5. 本项目属于集成平时开发的经验避免重复造轮子，所以如果在开发过程中遇到了本项目里面的一些 BUG，请及时在你的运行模块里面，新建
   BUG 类同包同名的类来强制重写本项目的代码，等到有空了把这些
   BUG，和你修复好的文件一起提交给本项目，将会是对本项目最伟大的贡献，感激不尽！另外，如果有好的点子，或者想法，也欢迎往本项目这儿疯狂
   PR.

#### 历史发行版本

[Release](https://mangocrisp.top/code/taybct/release/)

#### 免责声明

本项目所有依赖包都是互联网能找到的，不能保证没有漏洞，源码也不能百分百保证没有
BUG，谁都不能保证，所以，如果用于生产环境，出现了什么问题，本项目方不负任何责任，但是可以提供友情技术帮助和支持，你可以在项目的
Issues 提出你的问题