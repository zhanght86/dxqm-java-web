package jnpf.model;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 *
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021年6月15日
 */
@Data
public class VisualPagination {
    @ApiModelProperty(value = "每页条数",example = "10")
    private long size=10;
    @ApiModelProperty(value = "当前页数",example = "1")
    private long current=1;
    @ApiModelProperty(hidden = true)
    private long total;
    @ApiModelProperty(hidden = true)
    private long pages;

    public <T> List<T> setData(IPage<T> page) {
        this.total = page.getTotal();
        if (this.total > 0) {
            this.pages = this.total % this.size == 0 ? this.total / this.size : this.total / this.size + 1;
        } else {
            this.pages = 0L;
        }
        return page.getRecords();
    }
}
