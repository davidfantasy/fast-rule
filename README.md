# fast-rule

fast-rule是一款高性能的轻量级规则引擎，主要用于物联网等高并发领域。它功能简单易用，没有很多的复杂概念，但留出了足够的扩展点方便用户根据业务场景进行扩展。规则执行上采用**全异步设计**，且同时支持 **“PUSH”** 和 **“PULL”** 两种规则触发模式，性能足够能打。

# 设计方案
fast-rule的设计思路可以参考我写的这篇文章：
[从0到1手搓一个规则引擎（1）](https://mp.weixin.qq.com/s/yM8fjKokCnBo2zsFYsHokQ)
[从0到1手搓一个规则引擎（2）](https://mp.weixin.qq.com/s/GmDIZz3EUFM-eL17fY5ukQ)

# 快速开始

## 引入依赖：

```xml
<dependency>
    <groupId>com.github.davidfantasy</groupId>
    <artifactId>fast-rule</artifactId>
    <version>1.1.0</version>
</dependency>
```
## 简单示例

~~~java
//初始化规则引擎
RuleManager ruleManager = new DefaultRuleManager();
RuleEngine ruleEngine = new DefaultRuleEngine(ruleManager, RulesEngineConfig.builder().build());
ruleEngine.start();
//添加规则
ruleManager.add(new BaseRule("1", "rule1", 1, null, fact -> {
    int v = Integer.parseInt(fact.getValue("v").toString());
    return v > 5 && v < 10;
}) {
    @Override
    public void executeThen(Fact fact) {
        System.out.println("hit rule:" + fact.getId());
    }
});
//异步触发规则
ruleEngine.fire(new SimpleFact("1", "v", 7, System.currentTimeMillis()), false);
//关闭规则引擎
ruleEngine.shutdown();
~~~
更多的使用方式可以参考测试用例中的样本代码

## 配置
可通过RulesEngineConfig指定配置了多个规则时，规则引擎的匹配模式
~~~java
/**
 * 当fact已触发第一个规则后，是否跳过对后续的规则的匹配，仅当启用规则优先级匹配时生效
 */
private boolean skipOnFirstAppliedRule;
/**
 * 当fact没有触发第一个规则时，是否跳过对后续的规则的匹配，仅当启用规则优先级匹配时生效
 */
private boolean skipOnFirstNonAppliedRule;
/**
 * 当fact触发第一个规则执行发生异常时，是否跳过后续的规则，仅当启用规则优先级匹配时生效
 */
private boolean skipOnFirstFailedRule;
~~~

## 单次触发和延迟触发
框架提供了DelayStatefulTriggerRule，用于支持规则的单次触发模式和延迟触发模式，相关说明如下：
~~~java
/**
 * 有状态的规则，会保存规则的触发状态，触发中的规则不会被再次触发，直到某个fact不再满足规则条件重置规则的触发状态；
 * 举例：
 * rule1 当 fact a>5 时触发执行
 * 当规则引擎收到 5个采集的a的值时：
 * a=1 不满足，不会执行rule1
 * a=6 满足，执行rule1
 * a=8 满足，但不会执行rule1，因为前面已经触发过了
 * a=4 不满足，不会执行rule1，但会重置规则的执行状态
 * a=7 满足，执行rule1
 * 同时支持fact对规则的延迟触发，举例：
 * rule1 当 fact a>5 时触发执行，triggerDelayMS设置5000
 * 当规则引擎收到 4个采集的a的值时：
 * a=1 不满足，不会执行rule1
 * a=6 满足，加入到等待触发队列
 * a=8 满足，但a=6已加入等待触发队列，忽略该值
 * a=7 满足，但a=6已加入等待触发队列，忽略该值
 * 等待5000ms后，以a=6触发rule1的执行
 * 如果在5000ms内：
 * a=4 不满足，删除等待队列中a的值，到期后不会再执行
 **/
public abstract class DelayStatefulTriggerRule extends BaseRule {
    ......
}
~~~

## 脚本规则
目前支持基于Janino框架的表达式及脚本规则，使用方式如下：
~~~java
SimpleFact fact = new SimpleFact("fact1", "param", 20, null);
Map<String, Integer> resultMap = new HashMap<>();
fact.addValue("result", resultMap);
//规则的处理逻辑脚本
String executeScript = "result.put(\"number\", param*20);";
//规则的条件逻辑表达式
String conditionExpr = "param > 10";
Condition condition = new JaninoCondition(conditionExpr, new String[]{"param"}, new Class[]{Integer.class});
JaninoRule rule = new JaninoRule("rule1", "rule1", 1, "rule1", condition, executeScript, new String[]{"param", "result"}, new Class[]{Integer.class, Map.class});
ruleManager.add(rule);
ruleEngine.fire(fact, false);
Thread.sleep(100);
Assertions.assertEquals(400, resultMap.get("number"));
~~~

# 交流联系

使用上有问题请优先看一下单元测试中的例子，如果还不能解决请给我提issue，我会尽快处理。

也欢迎大家关注我的公众号（飞空之羽的技术手札），我会在上面定期分享一些关于技术的经验和感悟~

![二维码](https://github.com/davidfantasy/mybatis-plus-generator-ui/blob/master/imgs/wechat.jpg)