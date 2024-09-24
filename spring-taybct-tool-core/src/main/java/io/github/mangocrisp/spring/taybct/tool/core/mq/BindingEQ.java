package io.github.mangocrisp.spring.taybct.tool.core.mq;

/**
 * 这个类只能用来做示例了，不要打开这个 @AutoConfiguration
 * 如果有需要 配置，应该去自己的模块 配置和 mq 配置匹配的配置
 *
 * @author 24154
 */
//@AutoConfiguration
//@EnableRabbit
public class BindingEQ {

    /**
     * 定义
     */
    public interface def {
        /**
         * 前缀
         */
        String prefix = "MESSAGE";

        /**
         * 交换机
         */
        String exchange = "DELAYED_EXCHANGE";
        /**
         * 队列
         */
        String queue = "DELAYED_QUEUE";
        /**
         * 路由
         */
        String routingKey = "ROUTING_KEY";

    }

    public static String getName(String prefix, String key, String suffix) {
        return String.format("%s.%s.%s", prefix, key, suffix);
    }

    public static String getExchange(String key) {
        return getName(def.prefix, key, def.exchange);
    }

    public static String getQueue(String key) {
        return getName(def.prefix, key, def.queue);
    }

    public static String getRoutingKey(String key) {
        return getName(def.prefix, key, def.routingKey);
    }

    ////////////////////////// 布控

    /*
     * 交换机
     * 针对消费者配置
     * FanoutExchange: 将消息分发到所有的绑定队列，无routingkey的概念
     * HeadersExchange ：通过添加属性key-value匹配
     * DirectExchange:按照routingkey分发到指定队列
     * DirectExchange:多关键字匹配
     */

/*	@Bean
	public Queue queue() {
		boolean durable = true;
		boolean exclusive = false;
		boolean autoDelete = false;
		return new Queue(getQueue(def.sdms), durable, exclusive, autoDelete);
	}

	@Bean
	public DirectExchange defaultExchange() {
		boolean durable = true;
		boolean autoDelete = false;
		return new DirectExchange(getExchange(def.sdms), durable, autoDelete);
	}

	@Bean
	public Binding binding() {
		return BindingBuilder.bind(queue())
				.to(defaultExchange())
				.with(getRoutingKey(def.sdms));
	}*/

}
