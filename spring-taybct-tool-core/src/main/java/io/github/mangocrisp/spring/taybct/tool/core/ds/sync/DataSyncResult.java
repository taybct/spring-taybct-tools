package io.github.mangocrisp.spring.taybct.tool.core.ds.sync;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <pre>
 * 同步结果
 * </pre>
 *
 * @author XiJieYin
 * @since 2025/4/14 16:52
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(description = "同步结果")
@Builder
public class DataSyncResult implements Serializable {

    @Serial
    private static final long serialVersionUID = -8735259592037168857L;

    /**
     * 同步 key
     */
    @Schema(description = "key")
    private String key;
    /**
     * 同步结果
     */
    @Schema(description = "同步结果")
    private boolean ok = false;
    /**
     * 同步时间
     */
    @Schema(description = "同步时间")
    private LocalDateTime syncTime;
    /**
     * 同步结果信息
     */
    @Schema(description = "同步结果信息")
    private String message;
}
