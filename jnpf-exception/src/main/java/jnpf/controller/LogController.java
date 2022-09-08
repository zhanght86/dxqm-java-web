package jnpf.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.ActionResult;
import jnpf.base.PaginationTime;
import jnpf.base.vo.PaginationVO;
import jnpf.entity.LogEntity;
import jnpf.model.ErrorLogVO;
import jnpf.model.LogDelForm;
import jnpf.model.LoginLogVO;
import jnpf.model.RequestLogVO;
import jnpf.service.LogService;
import jnpf.util.JsonUtil;
import jnpf.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统日志
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Api(tags = "系统日志", value =  "Log")
@RestController
@RequestMapping("/api/system/Log")
public class LogController {

    @Autowired
    private LogService logService;


    /**
     * 获取系统日志信息
     *
     * @param category 主键值分类 1：登录日志，2.访问日志，3.操作日志，4.异常日志，5.请求日志
     * @return
     */
    @ApiOperation("获取系统日志列表")
    @GetMapping("/{category}")
    public ActionResult getInfoList(@PathVariable("category") String category, PaginationTime paginationTime) {
        if(StringUtil.isEmpty(category)||!StringUtil.isNumeric(category)){
            return ActionResult.fail("获取失败");
        }
        List<LogEntity> list = logService.getList(Integer.parseInt(category),paginationTime);
        PaginationVO paginationVO= JsonUtil.getJsonToBean(paginationTime,PaginationVO.class);
        int i=Integer.parseInt(category);
        switch (i){
            case 1:
                List<LoginLogVO> loginLogVOList= JsonUtil.getJsonToList(list,LoginLogVO.class);
                return ActionResult.page(loginLogVOList,paginationVO);
            case 4:
                List<ErrorLogVO> errorLogVOList= JsonUtil.getJsonToList(list,ErrorLogVO.class);
                for (int j = 0;j<errorLogVOList.size();j++){
                    errorLogVOList.get(j).setJson(list.get(j).getJsons());
                }
                return ActionResult.page(errorLogVOList,paginationVO);
            case 5:
                List<RequestLogVO> requestLogVOList= JsonUtil.getJsonToList(list,RequestLogVO.class);
                return ActionResult.page(requestLogVOList,paginationVO);
            default:
                return ActionResult.fail("获取失败");
        }
    }

    /**
     * 批量删除系统日志
     *
     * @return
     */
    @ApiOperation("批量删除系统日志")
    @DeleteMapping
    public ActionResult delete(@RequestBody LogDelForm logDelForm) {
        boolean flag=logService.delete(logDelForm.getIds());
        if(flag==false){
            return ActionResult.fail("删除失败，数据不存在");
        }
        return ActionResult.success("删除成功");
    }
}
