package io.github.taybct.tool.core.support;

import java.util.function.Function;

/**
 * 加密的数据有效性检查
 *
 * @author XiJieYin <br> 2024/5/9 13:39
 */
@FunctionalInterface
public interface IEncryptedPassable extends Function<String, String> {
}
