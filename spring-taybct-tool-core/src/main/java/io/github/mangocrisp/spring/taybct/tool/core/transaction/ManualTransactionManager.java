package io.github.mangocrisp.spring.taybct.tool.core.transaction;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * 手动事务
 *
 * @author XiJieYin <br> 2023/4/27 15:40
 */
@RequiredArgsConstructor
public class ManualTransactionManager {

    final PlatformTransactionManager transactionManager;

    /**
     * 开启事务，并发回一个事务 TransactionStatus，后面可以通过这个 TransactionStatus 来操作事务提交或者回滚,这里使用默认传播性 PROPAGATION_REQUIRED
     *
     * @return tx key
     */
    public TransactionStatus start() {
        return start(TransactionDefinition.PROPAGATION_REQUIRED);
    }

    /**
     * 开启事务，并发回一个事务 TransactionStatus ，后面可以通过这个 TransactionStatus 来操作事务提交或者回滚
     * <br>
     * Spring中七种事务传播行为
     * <br>
     * PROPAGATION_REQUIRED	如果当前没有事务，就新建一个事务，如果已经存在一个事务中，加入到这个事务中。这是最常见的选择。
     * <br>
     * PROPAGATION_SUPPORTS	支持当前事务，如果当前没有事务，就以非事务方式执行。
     * <br>
     * PROPAGATION_MANDATORY	使用当前的事务，如果当前没有事务，就抛出异常。
     * <br>
     * PROPAGATION_REQUIRES_NEW	新建事务，如果当前存在事务，把当前事务挂起。
     * <br>
     * PROPAGATION_NOT_SUPPORTED	以非事务方式执行操作，如果当前存在事务，就把当前事务挂起。
     * <br>
     * PROPAGATION_NEVER	以非事务方式执行，如果当前存在事务，则抛出异常。
     * <br>
     * PROPAGATION_NESTED	如果当前存在事务，则在嵌套事务内执行。如果当前没有事务，则执行与PROPAGATION_REQUIRED类似的操作。
     *
     * @param propagationBehavior 事务传播行为
     * @return TransactionStatus
     * @see TransactionDefinition
     */
    public TransactionStatus start(int propagationBehavior) {
        //开启事务
        DefaultTransactionDefinition defaultTransactionDefinition = new DefaultTransactionDefinition();
        // 嵌套事务
        defaultTransactionDefinition.setPropagationBehavior(propagationBehavior);
        // 设置嵌套事务
        return transactionManager.getTransaction(defaultTransactionDefinition);
    }

    /**
     * 提交事务
     *
     * @param status TransactionStatus
     */
    public void commit(TransactionStatus status) {
        transactionManager.commit(status);
    }

    /**
     * 回滚事务
     *
     * @param status TransactionStatus
     */
    public void rollback(TransactionStatus status) {
        transactionManager.rollback(status);
    }

}
