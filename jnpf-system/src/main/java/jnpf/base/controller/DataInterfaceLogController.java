package jnpf.base.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.ActionResult;
import jnpf.base.Pagination;
import jnpf.base.entity.DataInterfaceLogEntity;
import jnpf.base.model.dataInterface.DataInterfaceLogVO;
import jnpf.base.service.DataInterfaceLogService;
import jnpf.base.vo.PaginationVO;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 数据接口调用日志控制器
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-06-03
 */
@Api(value = "数据接口调用日志", tags = "DataInterfaceLog")
@RestController
@RequestMapping("/api/system/DataInterfaceLog")
public class DataInterfaceLogController {
    @Autowired
    private DataInterfaceLogService dataInterfaceLogService;
    @Autowired
    private UserService userService;

    /**
     * 获取数据接口调用日志列表
     *
     * @return
     */
    @ApiOperation("获取数据接口调用日志列表")
    @GetMapping("{id}")
    public ActionResult getList(@PathVariable("id") String id, Pagination pagination) {
        List<DataInterfaceLogEntity> list = dataInterfaceLogService.getList(id, pagination);
        List<DataInterfaceLogVO> voList = JsonUtil.getJsonToList(list, DataInterfaceLogVO.class);
        for (DataInterfaceLogVO vo : voList) {
            UserEntity entity = userService.getInfo(vo.getUserId());
            if (entity!=null){
                vo.setUserId(entity.getRealName() + "/" + entity.getAccount());
            }
        }
        PaginationVO vo = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
        return ActionResult.page(voList, vo);
    }
}
