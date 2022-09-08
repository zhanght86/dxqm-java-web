package jnpf.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.ActionResult;
import jnpf.base.vo.ListVO;
import jnpf.entity.ProductclassifyEntity;
import jnpf.model.productclassify.*;
import jnpf.service.ProductclassifyService;
import jnpf.util.JsonUtil;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.newtreeutil.TreeDotUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 产品分类
 *
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021-07-10 14:34:04
 */
@Slf4j
@RestController
@Api(tags = "产品分类", value = "Classify")
@RequestMapping("/api/extend/saleOrder/Classify")
public class ProductclassifyController {

    @Autowired
    private ProductclassifyService productclassifyService;

    /**
     * 列表
     *
     * @param
     * @return
     */
    @GetMapping
    @ApiOperation("列表")
    public ActionResult list() {
        List<ProductclassifyEntity> data = productclassifyService.getList();
        List<ProductclassifyModel> modelList = JsonUtil.getJsonToList(data, ProductclassifyModel.class);
        List<SumTree<ProductclassifyModel>> sumTrees = TreeDotUtils.convertListToTreeDot(modelList);
        List<ProductclassifyListVO> list = JsonUtil.getJsonToList(sumTrees, ProductclassifyListVO.class);
        ListVO vo = new ListVO();
        vo.setList(list);
        return ActionResult.success(vo);
    }

    /**
     * 创建
     *
     * @param classifyCrForm
     * @return
     */
    @PostMapping
    @ApiOperation("创建")
    public ActionResult create(@RequestBody @Valid ProductclassifyCrForm classifyCrForm) {
        ProductclassifyEntity entity = JsonUtil.getJsonToBean(classifyCrForm, ProductclassifyEntity.class);
        productclassifyService.create(entity);
        return ActionResult.success("新建成功");
    }

    /**
     * 信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("信息")
    public ActionResult<ProductclassifyInfoVO> info(@PathVariable("id") String id) {
        ProductclassifyEntity entity = productclassifyService.getInfo(id);
        ProductclassifyInfoVO vo = JsonUtil.getJsonToBean(entity, ProductclassifyInfoVO.class);
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
    public ActionResult update(@PathVariable("id") String id, @RequestBody @Valid ProductclassifyUpForm classifyUpForm) {
        ProductclassifyEntity entity = JsonUtil.getJsonToBean(classifyUpForm, ProductclassifyEntity.class);
        boolean ok = productclassifyService.update(id, entity);
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
        ProductclassifyEntity entity = productclassifyService.getInfo(id);
        if (entity != null) {
            productclassifyService.delete(entity);
        }
        return ActionResult.success("删除成功");
    }

}
