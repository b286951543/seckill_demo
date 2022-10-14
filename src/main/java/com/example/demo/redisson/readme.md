该 redisson 的 DelayedQueue 队列可用于处理过期订单问题

关于 Redisson 的 DelayedQueue 队列

延时队列的具体实现涉及三个队列和一个发布订阅通道

* 阻塞队列 List：KEY = queueName，执行 BLPOP 命令从左端弹出元素，右端插入元素。当一条数据到达过期时间的时候，会从redisson_delay_queue:{DelayMessage}中移除，加入到这个队列，客户端监听的就是这个队列，这个队列里面的全都是已经过期的数据。
* 有序集合 Sorted Set：KEY = redisson_delay_queue_timeout:{queueName}，score 是元素的过期时间，按从小到大排序，过期时间小于当前时间表示已过期，删除集合中的元素，同时将普通集合 List中对应的元素删除，并将元素添加到阻塞队列 List等待客户端消费。
* 普通集合 List：KEY = redisson_delay_queue:{DelayMessage}，按顺序从右端添加元素，元素过期会被删除。
* 发布/订阅通道：redisson_delay_queue_channel，往延时队列中放入一个数据时，会将延时时间publish出去，客户端收到之后会按这个时间延时之后再执行定时任务。

原理：《Redisson 延时队列 原理 详解.md》

来源：https://blog.csdn.net/u012684638/article/details/103407931