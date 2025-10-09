package io.github.mangocrisp.spring.taybct.tool.core.version;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.servlet.mvc.condition.RequestCondition;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * api 版本控制请求规则配置
 *
 * @param apiVersion 类中的成员变量，此处表示到时候接口中传递的参数接收
 * @author xijieyin <br> 2022/10/14 15:14
 * @since 1.0.5
 */
public record ApiVersionCondition(int apiVersion) implements RequestCondition<ApiVersionCondition> , Serializable {

    /**
     * 在路径中直接匹配 /v1/ 类似这样的，如果是直接 /v1 这样结尾的 RESTFul 接口，也没关系默认加个 / 就好了
     */
    private final static Pattern VERSION_PATTERN = Pattern.compile("/v(\\d+)/");

    /**
     * 创建新的实例，和另外一个请求匹配条件合并，具体合并逻辑由实现类提供
     *
     * @return ApiVersionCondition
     * @author xijieyin <br> 2022/10/14 15:16
     * @since 1.0.5
     */
    @NotNull
    @Override
    public ApiVersionCondition combine(ApiVersionCondition other) {
        // 采用最后定义优先原则，则方法上的定义覆盖类上面的定义
        return new ApiVersionCondition(other.apiVersion());
    }

    /**
     * 检查当前请求匹配条件和指定请求request是否匹配，如果不匹配返回null，
     * 如果匹配，生成一个新的请求匹配条件，该新的请求匹配条件是当前请求匹配条件
     * 针对指定请求request的剪裁。
     * 举个例子来讲，如果当前请求匹配条件是一个路径匹配条件，包含多个路径匹配模板，
     * 并且其中有些模板和指定请求request匹配，那么返回的新建的请求匹配条件将仅仅
     * 包含和指定请求request匹配的那些路径模板。
     *
     * @return ApiVersionCondition
     * @author xijieyin <br> 2022/10/14 15:16
     * @since 1.0.5
     */
    @Override
    public ApiVersionCondition getMatchingCondition(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (!uri.endsWith("/")) {
            uri = uri + "/";
        }
        Matcher m = VERSION_PATTERN.matcher(uri);
        if (m.find()) {
            int version = Integer.parseInt(m.group(1));
            // 如果当前 url 中传递的版本信息高于(或等于)申明(或默认)版本，则用 url 的版本
            if (version >= this.apiVersion) {
                return this;
            }
        }
        // 不匹配，就说明找不到对应版本的接口
        return null;
    }

    /**
     * 针对指定的请求对象request比较两个请求匹配条件。
     * 该方法假定被比较的两个请求匹配条件都是针对该请求对象request调用了
     * #getMatchingCondition 方法得到的，这样才能确保对它们的比较
     * 是针对同一个请求对象request，这样的比较才有意义(最终用来确定谁是更匹配的条件)。
     *
     * @return int
     * @author xijieyin <br> 2022/10/14 15:19
     * @since 1.0.5
     */
    @Override
    public int compareTo(ApiVersionCondition other, @NotNull HttpServletRequest request) {
        // 优先匹配最新的版本号
        return other.apiVersion() - this.apiVersion;
    }

}
