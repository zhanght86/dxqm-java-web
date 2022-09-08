package jnpf.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:54
 */
@Data
public class Pagination extends Page{
    @ApiModelProperty(value = "每页条数",example = "20")
    private long pageSize=20;
    @ApiModelProperty(value = "排序类型")
    private String sort="desc";
    @ApiModelProperty(value = "排序列")
    private String sidx="";
    @ApiModelProperty(value = "当前页数",example = "1")
    private long currentPage=1;



    @ApiModelProperty(hidden = true)
    @JsonIgnore
    private long total;
    @ApiModelProperty(hidden = true)
    @JsonIgnore
    private long records;

    public <T> List<T> setData(List<T> data, long records) {
        this.total = records;
        return data;
    }
}
