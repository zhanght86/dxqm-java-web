package jnpf.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.ActionResult;
import jnpf.base.vo.ListVO;
import jnpf.entity.AppDataEntity;
import jnpf.model.*;
import jnpf.service.AppDataService;
import jnpf.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * app常用数据
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-07-08
 */
@Api(tags = "app常用数据", value = "data")
@RestController
@RequestMapping("/api/app/Data")
public class AppDataController {

    @Autowired
    private AppDataService appDataService;

    /**
     * 常用数据
     *
     * @return
     */
    @ApiOperation("常用数据")
    @GetMapping
    public ActionResult<ListVO<AppDataListVO>> list(String type) {
        List<AppDataEntity> list = appDataService.getList(type);
        List<AppDataListVO> data = JsonUtil.getJsonToList(list, AppDataListVO.class);
        ListVO listVO = new ListVO();
        listVO.setList(data);
        return ActionResult.success(listVO);
    }

    /**
     * 新建
     *
     * @param appDataCrForm dto实体
     * @return
     */
    @PostMapping
    @ApiOperation("新建")
    public ActionResult create(@RequestBody @Valid AppDataCrForm appDataCrForm) {
        AppDataEntity entity = JsonUtil.getJsonToBean(appDataCrForm, AppDataEntity.class);
        if (appDataService.isExistByObjectId(entity.getObjectId())) {
            return ActionResult.fail("常用数据已存在");
        }
        appDataService.create(entity);
        return ActionResult.success("创建成功");
    }

    /**
     * 删除
     *
     * @param objectId 对象主键
     * @return
     */
    @ApiOperation("删除")
    @DeleteMapping("/{objectId}")
    public ActionResult create(@PathVariable("objectId") String objectId) {
        AppDataEntity entity = appDataService.getInfo(objectId);
        if (entity != null) {
            appDataService.delete(entity);
            return ActionResult.success("删除成功");
        }
        return ActionResult.fail("删除失败，数据不存在");
    }

    /**
     * 所有流程
     *
     * @return
     */
    @ApiOperation("所有流程")
    @GetMapping("/getFlowList")
    public ActionResult<ListVO<AppFlowListAllVO>> getFlowList() {
        List<AppFlowListAllVO> result = appDataService.getFlowList("1");
        ListVO listVO = new ListVO();
        listVO.setList(result);
        return ActionResult.success(listVO);
    }

    /**
     * 所有应用
     *
     * @return
     */
    @ApiOperation("所有应用")
    @GetMapping("/getDataList")
    public ActionResult<ListVO<AppDataListAllVO>> getAllList() {
        List<AppDataListAllVO> result = appDataService.getDataList("2");
        ListVO listVO = new ListVO();
        listVO.setList(result);
        return ActionResult.success(listVO);
    }

}
