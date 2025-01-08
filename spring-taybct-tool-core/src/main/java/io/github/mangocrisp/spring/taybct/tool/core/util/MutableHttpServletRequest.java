package io.github.mangocrisp.spring.taybct.tool.core.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.*;

/**
 * 定义Request包装类
 *
 * @author xijieyin <br> 2022/8/4 20:24
 * @since 1.0.0
 */
public class MutableHttpServletRequest extends HttpServletRequestWrapper {
    private final Map<String, String> customHeaders;

    public MutableHttpServletRequest(HttpServletRequest request) {
        super(request);
        this.customHeaders = new HashMap<>();
    }

    public void putHeader(String name, String value) {
        this.customHeaders.put(name, value);
    }

    @Override
    public String getHeader(String name) {
        return Optional.ofNullable(customHeaders.get(name)).orElse(((HttpServletRequest) getRequest()).getHeader(name));
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        if (customHeaders.isEmpty()) {
            return super.getHeaderNames();
        }

        Set<String> set = new HashSet<>(customHeaders.keySet());
        // 添加自定义header
        Enumeration<String> e = ((HttpServletRequest) getRequest()).getHeaderNames();
        while (e.hasMoreElements()) {
            String n = e.nextElement();
            set.add(n);
        }

        return Collections.enumeration(set);
    }
}
