package io.github.mangocrisp.spring.taybct.tool.core.tpa;

public interface ApiConfig {

    /**
     * 缓存 token 信息的 key，可以是用户 id ，或者是客户端 id 这样的
     *
     * @return key
     */
    String getTokenCacheKey();
}
