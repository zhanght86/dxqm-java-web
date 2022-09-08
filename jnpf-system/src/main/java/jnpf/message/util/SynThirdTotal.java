package jnpf.message.util;

import lombok.Data;

import java.util.Date;

/**
 * 同步统计信息模型
 *
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021/5/10 11:21
 */
@Data
public class SynThirdTotal {
    /**
     * 同步类型
     */
    private String synType;
    /**
     * 记录总数
     */
    private Integer recordTotal;
    /**
     * 同步成功记录数
     */
    private Long synSuccessCount;
    /**
     * 同步失败记录数
     */
    private Long synFailCount;
    /**
     * 未同步记录数
     */
    private Long unSynCount;
    /**
     * 最后同步时间
     */
    private Date synDate;

}
