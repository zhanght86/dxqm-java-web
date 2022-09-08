package jnpf.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.ActionResult;
import jnpf.base.Pagination;
import jnpf.base.vo.PageListVO;
import jnpf.entity.CustomerEntity;
import jnpf.model.customer.CustomerCrForm;
import jnpf.model.customer.CustomerInfoVO;
import jnpf.model.customer.CustomerListVO;
import jnpf.model.customer.CustomerUpForm;
import jnpf.service.CustomerService;
import jnpf.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 客户信息
 *
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021-07-10 14:09:05
 */
@Slf4j
@RestController
@Api(tags = "客户信息", value = "Customer")
@RequestMapping("/api/extend/saleOrder/Customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    /**
     * 列表
     *
     * @param pagination
     * @return
     */
    @GetMapping
    @ApiOperation("列表")
    public ActionResult list(Pagination pagination) {
        pagination.setPageSize(50);
        pagination.setCurrentPage(1);
        List<CustomerEntity> list = customerService.getList(pagination);
        List<CustomerListVO> listVO = JsonUtil.getJsonToList(list, CustomerListVO.class);
        PageListVO vo = new PageListVO();
        vo.setList(listVO);
        return ActionResult.success(vo);
    }

    /**
     * 创建
     *
     * @param customerCrForm
     * @return
     */
    @PostMapping
    @ApiOperation("创建")
    public ActionResult create(@RequestBody @Valid CustomerCrForm customerCrForm) {
        CustomerEntity entity = JsonUtil.getJsonToBean(customerCrForm, CustomerEntity.class);
        customerService.create(entity);
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
    public ActionResult<CustomerInfoVO> info(@PathVariable("id") String id) {
        CustomerEntity entity = customerService.getInfo(id);
        CustomerInfoVO vo = JsonUtil.getJsonToBean(entity, CustomerInfoVO.class);
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
    public ActionResult update(@PathVariable("id") String id, @RequestBody @Valid CustomerUpForm customerUpForm) {
        CustomerEntity entity = JsonUtil.getJsonToBean(customerUpForm, CustomerEntity.class);
        boolean ok = customerService.update(id, entity);
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
        CustomerEntity entity = customerService.getInfo(id);
        if (entity != null) {
            customerService.delete(entity);
        }
        return ActionResult.success("删除成功");
    }


}
