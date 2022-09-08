package jnpf.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.ActionResult;
import jnpf.base.vo.PageListVO;
import jnpf.entity.VisualDbEntity;
import jnpf.model.VisualPageVO;
import jnpf.model.VisualPagination;
import jnpf.model.visualdb.*;
import jnpf.model.visualcategory.VisualCategoryInfoVO;
import jnpf.service.VisualDbService;
import jnpf.util.JsonUtil;
import jnpf.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 大屏数据源配置
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021年6月15日
 */
@RestController
@Api(tags = "大屏数据源配置", value = "db")
@RequestMapping("/api/blade-visual/db")
public class VisualDbController {

    @Autowired
    private VisualDbService dbService;

    /**
     * 分页
     *
     * @param pagination
     * @return
     */
    @ApiOperation("分页")
    @GetMapping("/list")
    public ActionResult<PageListVO<VisualDbListVO>> list(VisualPagination pagination) {
        List<VisualDbEntity> data = dbService.getList(pagination);
        List<VisualDbListVO> list = JsonUtil.getJsonToList(data, VisualDbListVO.class);
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
    public ActionResult<VisualCategoryInfoVO> info(String id) {
        VisualDbEntity entity = dbService.getInfo(id);
        VisualDbInfoVO vo = JsonUtil.getJsonToBean(entity, VisualDbInfoVO.class);
        return ActionResult.success(vo);
    }

    /**
     * 新增或修改
     *
     * @param dbUpForm
     * @return
     */
    @ApiOperation("新增或修改")
    @PostMapping("/submit")
    public ActionResult submit(@RequestBody VisualDbUpForm dbUpForm) {
        VisualDbEntity entity = JsonUtil.getJsonToBean(dbUpForm, VisualDbEntity.class);
        if (StringUtil.isEmpty(entity.getId())) {
            dbService.create(entity);
            return ActionResult.success("新建成功");
        } else {
            dbService.update(entity.getId(), entity);
            return ActionResult.success("更新成功");
        }
    }

    /**
     * 新增
     *
     * @param dbCrForm
     * @return
     */
    @ApiOperation("新增")
    @PostMapping("/save")
    public ActionResult create(@RequestBody VisualDbCrForm dbCrForm) {
        VisualDbEntity entity = JsonUtil.getJsonToBean(dbCrForm, VisualDbEntity.class);
        dbService.create(entity);
        return ActionResult.success("新建成功");
    }

    /**
     * 修改
     *
     * @param dbUpForm
     * @return
     */
    @ApiOperation("修改")
    @PostMapping("/update")
    public ActionResult update(@RequestBody VisualDbUpForm dbUpForm) {
        VisualDbEntity entity = JsonUtil.getJsonToBean(dbUpForm, VisualDbEntity.class);
        dbService.update(entity.getId(), entity);
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
        VisualDbEntity entity = dbService.getInfo(ids);
        if (entity != null) {
            dbService.delete(entity);
            return ActionResult.success("删除成功");
        }
        return ActionResult.fail("删除失败，数据不存在");
    }

    /**
     * 下拉数据源
     *
     * @return
     */
    @ApiOperation("下拉数据源")
    @GetMapping("/db-list")
    public ActionResult<VisualDbSelectVO> list() {
        List<VisualDbEntity> data = dbService.getList();
        List<VisualDbSelectVO> list = JsonUtil.getJsonToList(data, VisualDbSelectVO.class);
        return ActionResult.success(list);
    }

    /**
     * 数据源测试连接
     *
     * @param dbCrForm
     * @return
     */
    @ApiOperation("数据源测试连接")
    @PostMapping("/db-test")
    public ActionResult test(@RequestBody VisualDbCrForm dbCrForm) {
        VisualDbEntity entity = JsonUtil.getJsonToBean(dbCrForm, VisualDbEntity.class);
        boolean flag = dbService.dbTest(entity);
        if (flag) {
            return ActionResult.success("连接成功");
        }
        return ActionResult.fail("连接失败");
    }

    /**
     * 动态执行SQL
     *
     * @param queryForm
     * @return
     */
    @ApiOperation("动态执行SQL")
    @PostMapping("/dynamic-query")
    public ActionResult query(@RequestBody VisualDbQueryForm queryForm) {
        VisualDbEntity entity = dbService.getInfo(queryForm.getId());
        List<Map<String, Object>> data = new ArrayList<>();
        if (entity != null) {
            data = dbService.query(entity, queryForm.getSql());
        }
        return ActionResult.success(data);
    }

}
