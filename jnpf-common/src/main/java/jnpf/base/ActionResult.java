package jnpf.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:47
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionResult<T> {

    @ApiModelProperty("状态码")
    private Integer code;

    @ApiModelProperty("返回信息")
    private String msg;

    @ApiModelProperty("返回数据")
    private T data;

    public static ActionResult success() {
        ActionResult jsonData = new ActionResult();
        jsonData.setCode(200);
        jsonData.setMsg("Success");
        return jsonData;
    }

    public static ActionResult success(String msg) {
        ActionResult jsonData = new ActionResult();
        jsonData.setCode(200);
        jsonData.setMsg(msg);
        return jsonData;
    }

    public static ActionResult success(Object rows , PageModel pageModel){
        ActionResult jsonData = new ActionResult();
        Map<String, Object> map = new HashMap<>(16);
        map.put("page",pageModel.getPage());
        map.put("records",pageModel.getRecords());
        map.put("rows",rows);
        map.put("total",pageModel.getTotal());
        jsonData.setData(map);
        jsonData.setCode(200);
        jsonData.setMsg("Success");
        return jsonData;
    }

    public static ActionResult success(Object object) {
        ActionResult jsonData = new ActionResult();
        jsonData.setData(object);
        jsonData.setCode(200);
        jsonData.setMsg("Success");
        return jsonData;
    }
    public static<T> ActionResult page(List<T> list, PaginationVO pagination) {
        ActionResult jsonData = new ActionResult();
        PageListVO<T> vo = new PageListVO<>();
        vo.setList(list);
        vo.setPagination(pagination);
        jsonData.setData(vo);
        jsonData.setCode(200);
        jsonData.setMsg("Success");
        return jsonData;
    }

    public static ActionResult success(String msg, Object object) {
        ActionResult jsonData = new ActionResult();
        jsonData.setData(object);
        jsonData.setCode(200);
        jsonData.setMsg(msg);
        return jsonData;
    }

    public static ActionResult fail(Integer code, String message) {
        ActionResult jsonData = new ActionResult();
        jsonData.setCode(code);
        jsonData.setMsg(message);
        return jsonData;
    }

    public static ActionResult fail(String msg, String data) {
        ActionResult jsonData = new ActionResult();
        jsonData.setMsg(msg);
        jsonData.setData(data);
        return jsonData;
    }

    public static ActionResult fail(String msg) {
        ActionResult jsonData = new ActionResult();
        jsonData.setMsg(msg);
        jsonData.setCode(400);
        return jsonData;
    }
}
