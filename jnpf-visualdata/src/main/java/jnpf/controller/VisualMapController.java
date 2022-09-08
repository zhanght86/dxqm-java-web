package jnpf.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.ActionResult;
import jnpf.base.vo.PageListVO;
import jnpf.entity.VisualMapEntity;
import jnpf.model.VisualPageVO;
import jnpf.model.VisualPagination;
import jnpf.model.visualmap.VisualMapCrForm;
import jnpf.model.visualmap.VisualMapInfoVO;
import jnpf.model.visualmap.VisualMapListVO;
import jnpf.model.visualmap.VisualMapUpForm;
import jnpf.model.visualcategory.VisualCategoryListVO;
import jnpf.service.VisualMapService;
import jnpf.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
 * 大屏地图
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021年6月15日
 */
@RestController
@Api(tags = "大屏地图", value = "map")
@RequestMapping("/api/blade-visual/map")
public class VisualMapController {

    @Autowired
    private VisualMapService mapService;

    /**
     * 分页
     *
     * @param pagination
     * @return
     */
    @ApiOperation("分页")
    @GetMapping("/list")
    public ActionResult<PageListVO<VisualMapListVO>> list(VisualPagination pagination) {
        List<VisualMapEntity> data = mapService.getList(pagination);
        List<VisualMapListVO> list = JsonUtil.getJsonToList(data, VisualMapListVO.class);
        VisualPageVO paginationVO = JsonUtil.getJsonToBean(pagination, VisualPageVO.class);
        paginationVO.setRecords(list);
        return ActionResult.success(paginationVO);
    }

    /**
     * 详情
     *
     * @param id
     * @return
     */
    @ApiOperation("详情")
    @GetMapping("/detail")
    public ActionResult<VisualMapInfoVO> info(String id) {
        VisualMapEntity entity = mapService.getInfo(id);
        VisualMapInfoVO vo = JsonUtil.getJsonToBean(entity, VisualMapInfoVO.class);
        return ActionResult.success(vo);
    }

    /**
     * 新增
     *
     * @param mapCrForm
     * @return
     */
    @ApiOperation("新增")
    @PostMapping("/save")
    public ActionResult create(@RequestBody VisualMapCrForm mapCrForm) {
        VisualMapEntity entity = JsonUtil.getJsonToBean(mapCrForm, VisualMapEntity.class);
        mapService.create(entity);
        return ActionResult.success("新建成功");
    }

    /**
     * 修改
     *
     * @param mapUpForm
     * @return
     */
    @ApiOperation("修改")
    @PostMapping("/update")
    public ActionResult update(@RequestBody VisualMapUpForm mapUpForm) {
        VisualMapEntity entity = JsonUtil.getJsonToBean(mapUpForm, VisualMapEntity.class);
        boolean flag = mapService.update(mapUpForm.getId(), entity);
        if (!flag) {
            return ActionResult.fail("更新失败，数据不存在");
        }
        return ActionResult.success("更新成功");
    }

    /**
     * 删除
     *
     * @param ids
     * @return
     */
    @ApiOperation("删除")
    @PostMapping("/remove")
    public ActionResult delete(String ids) {
        VisualMapEntity entity = mapService.getInfo(ids);
        if (entity != null) {
            mapService.delete(entity);
            return ActionResult.success("删除成功");
        }
        return ActionResult.fail("删除失败，数据不存在");
    }

    /**
     * 数据详情
     *
     * @param id
     * @return
     */
    @ApiOperation("数据详情")
    @GetMapping("/data")
    public Map<String,Object> dataInfo(String id) {
        VisualMapEntity entity = mapService.getInfo(id);
        Map<String,Object> data = JsonUtil.stringToMap(entity.getData());
        return data;
    }

}
