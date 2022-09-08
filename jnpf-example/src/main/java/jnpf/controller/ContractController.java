package jnpf.controller;

import jnpf.base.ActionResult;
import jnpf.base.Pagination;
import jnpf.base.vo.PaginationVO;
import jnpf.service.ContractService;
import jnpf.model.ContractForm;
import jnpf.model.ContractInfoVO;
import jnpf.model.ContractListVO;
import jnpf.entity.ContractEntity;
import jnpf.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 9:47
 */
@RestController
@RequestMapping("/Contract")
public class ContractController {

    @Autowired
    private ContractService contractService;

    @GetMapping("/List")
    public ActionResult list(Pagination pagination) {
        List<ContractEntity> entity = contractService.getlist(pagination);
        List<ContractListVO> listVo = JsonUtil.getJsonToList(JsonUtil.getObjectToStringDateFormat(entity, "yyyy-MM-dd HH:mm:ss"),ContractListVO.class );
        PaginationVO vo = JsonUtil.getJsonToBean(pagination,PaginationVO.class);
        return ActionResult.page(listVo,vo);
    }

    @GetMapping("/{id}")
    public ActionResult info(@PathVariable("id") String id) {
        ContractEntity entity = contractService.getInfo(id);
        ContractInfoVO vo = JsonUtil.getJsonToBean(entity, ContractInfoVO.class);
        return ActionResult.success(vo);
    }

    @PostMapping
    public ActionResult create(@RequestBody @Valid ContractForm contractForm) {
        ContractEntity entity = JsonUtil.getJsonToBean(contractForm, ContractEntity.class);
        contractService.create(entity);
        return ActionResult.success("保存成功");
    }

    @PutMapping("/{id}")
    public ActionResult update(@PathVariable("id") String id,@RequestBody @Valid ContractForm contractForm) {
        ContractEntity entity = JsonUtil.getJsonToBean(contractForm, ContractEntity.class);
        contractService.update(id,entity);
        return ActionResult.success("修改成功");
    }

    @DeleteMapping("/{id}")
    public ActionResult delete(@PathVariable("id") String id) {
        ContractEntity entity = contractService.getInfo(id);
        contractService.delete(entity);
        return ActionResult.success("删除成功");
    }

}
