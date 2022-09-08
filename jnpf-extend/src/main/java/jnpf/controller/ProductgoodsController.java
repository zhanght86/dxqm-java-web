package jnpf.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.ActionResult;
import jnpf.base.vo.ListVO;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.entity.ProductgoodsEntity;
import jnpf.model.productgoods.*;
import jnpf.service.ProductgoodsService;
import jnpf.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 产品商品
 *
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021-07-10 15:57:50
 */
@Slf4j
@RestController
@Api(tags = "产品商品", value = "Goods")
@RequestMapping("/api/extend/saleOrder/Goods")
public class ProductgoodsController {

    @Autowired
    private ProductgoodsService productgoodsService;

    /**
     * 列表
     *
     * @param type
     * @return
     */
    @GetMapping("/getGoodList")
    @ApiOperation("列表")
    public ActionResult list(String type) {
        List<ProductgoodsEntity> list = productgoodsService.getGoodList(type);
        List<ProductgoodsListVO> listVO =JsonUtil.getJsonToList(list, ProductgoodsListVO.class);
        ListVO vo = new ListVO();
        vo.setList(listVO);
        return ActionResult.success(vo);
    }

    /**
     * 列表
     *
     * @param goodsPagination
     * @return
     */
    @GetMapping
    @ApiOperation("列表")
    public ActionResult list(ProductgoodsPagination goodsPagination) {
        List<ProductgoodsEntity> list = productgoodsService.getList(goodsPagination);
        List<ProductgoodsListVO> listVO = JsonUtil.getJsonToList(list, ProductgoodsListVO.class);
        PageListVO vo = new PageListVO();
        vo.setList(listVO);
        PaginationVO page = JsonUtil.getJsonToBean(goodsPagination, PaginationVO.class);
        vo.setPagination(page);
        return ActionResult.success(vo);
    }

    /**
     * 创建
     *
     * @param goodsCrForm
     * @return
     */
    @PostMapping
    @ApiOperation("创建")
    public ActionResult create(@RequestBody @Valid ProductgoodsCrForm goodsCrForm) {
        ProductgoodsEntity entity = JsonUtil.getJsonToBean(goodsCrForm, ProductgoodsEntity.class);
        productgoodsService.create(entity);
        return ActionResult.success("新建成功");
    }

    /**
     * 信息
     *
     * @param id
     * @return
     */
    @ApiOperation("信息")
    @GetMapping("/{id}")
    public ActionResult<ProductgoodsInfoVO> info(@PathVariable("id") String id) {
        ProductgoodsEntity entity = productgoodsService.getInfo(id);
        ProductgoodsInfoVO vo = JsonUtil.getJsonToBean(entity, ProductgoodsInfoVO.class);
        return ActionResult.success(vo);
    }

    /**
     * 更新
     *
     * @param id
     * @return
     */
    @PutMapping("/{id}")
    @ApiOperation("更新")
    public ActionResult update(@PathVariable("id") String id, @RequestBody @Valid ProductgoodsUpForm goodsCrFormUpForm) {
        ProductgoodsEntity entity = JsonUtil.getJsonToBean(goodsCrFormUpForm, ProductgoodsEntity.class);
        boolean ok = productgoodsService.update(id, entity);
        if (ok) {
            return ActionResult.success("更新成功");
        }
        return ActionResult.fail("更新失败，数据不存在");
    }

    /**
     * 删除
     *
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除")
    public ActionResult delete(@PathVariable("id") String id) {
        ProductgoodsEntity entity = productgoodsService.getInfo(id);
        if (entity != null) {
            productgoodsService.delete(entity);
        }
        return ActionResult.success("删除成功");
    }

    /**
     * 下拉
     *
     * @param goodsPagination
     * @return
     */
    @GetMapping("/Selector")
    @ApiOperation("下拉")
    public ActionResult listSelect(ProductgoodsPagination goodsPagination) {
        goodsPagination.setCurrentPage(1);
        goodsPagination.setPageSize(50);
        List<ProductgoodsEntity> list = productgoodsService.getList(goodsPagination);
        List<ProductgoodsListVO> listVO = JsonUtil.getJsonToList(list, ProductgoodsListVO.class);
        ListVO vo = new ListVO();
        vo.setList(listVO);
        return ActionResult.success(vo);
    }

}
