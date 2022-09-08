package jnpf.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.ActionResult;
import jnpf.base.vo.PageListVO;
import jnpf.model.VisualPageVO;
import jnpf.model.VisualPagination;
import jnpf.model.visualcategory.VisualCategoryCrForm;
import jnpf.model.visualcategory.VisualCategoryInfoVO;
import jnpf.model.visualcategory.VisualCategoryListVO;
import jnpf.model.visualcategory.VisualCategoryUpForm;
import jnpf.util.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jnpf.entity.VisualCategoryEntity;
import jnpf.service.VisualCategoryService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.Valid;
import java.util.*;

/**
 * 大屏分类
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021年6月15日
 */
@RestController
@Api(tags = "大屏分类", value = "category")
@RequestMapping("/api/blade-visual/category")
public class VisualCategoryController {

    @Autowired
    private VisualCategoryService categoryService;

    /**
     * 分页
     *
     * @param pagination
     * @return
     */
    @ApiOperation("分页")
    @GetMapping("/page")
    public ActionResult<PageListVO<VisualCategoryListVO>> list(VisualPagination pagination) {
        List<VisualCategoryEntity> data = categoryService.getList(pagination);
        List<VisualCategoryListVO> list = JsonUtil.getJsonToList(data, VisualCategoryListVO.class);
        VisualPageVO paginationVO = JsonUtil.getJsonToBean(pagination, VisualPageVO.class);
        paginationVO.setRecords(list);
        return ActionResult.success(paginationVO);
    }

    /**
     * 列表
     *
     * @return
     */
    @ApiOperation("列表")
    @GetMapping("/list")
    public ActionResult<VisualCategoryListVO> list() {
        List<VisualCategoryEntity> data = categoryService.getList();
        List<VisualCategoryListVO> list = JsonUtil.getJsonToList(data, VisualCategoryListVO.class);
        return ActionResult.success(list);
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
        VisualCategoryEntity entity = categoryService.getInfo(id);
        VisualCategoryInfoVO vo = JsonUtil.getJsonToBean(entity, VisualCategoryInfoVO.class);
        return ActionResult.success(vo);
    }

    /**
     * 新增
     *
     * @param categoryCrForm
     * @return
     */
    @ApiOperation("新增")
    @PostMapping("/save")
    public ActionResult create(@RequestBody @Valid VisualCategoryCrForm categoryCrForm) {
        VisualCategoryEntity entity = JsonUtil.getJsonToBean(categoryCrForm, VisualCategoryEntity.class);
        if (categoryService.isExistByValue(entity.getCategoryvalue(), entity.getId())) {
            return ActionResult.fail("模块键值已存在");
        }
        categoryService.create(entity);
        return ActionResult.success("新建成功");
    }

    /**
     * 修改
     *
     * @param categoryUpForm
     * @return
     */
    @ApiOperation("修改")
    @PostMapping("/update")
    public ActionResult update(@RequestBody VisualCategoryUpForm categoryUpForm) {
        VisualCategoryEntity entity = JsonUtil.getJsonToBean(categoryUpForm, VisualCategoryEntity.class);
        if (categoryService.isExistByValue(entity.getCategoryvalue(), entity.getId())) {
            return ActionResult.fail("模块键值已存在");
        }
        boolean flag = categoryService.update(categoryUpForm.getId(), entity);
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
        VisualCategoryEntity entity = categoryService.getInfo(ids);
        if (entity != null) {
            categoryService.delete(entity);
            return ActionResult.success("删除成功");
        }
        return ActionResult.fail("删除失败，数据不存在");
    }

}
