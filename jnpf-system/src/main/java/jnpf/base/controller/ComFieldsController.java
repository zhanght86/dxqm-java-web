package jnpf.base.controller;

import io.swagger.annotations.Api;
import jnpf.base.ActionResult;
import jnpf.base.service.ComFieldsService;
import jnpf.base.vo.ListVO;
import jnpf.base.entity.ComFieldsEntity;
import jnpf.database.exception.DataException;
import jnpf.base.model.comfields.ComFieldsCrForm;
import jnpf.base.model.comfields.ComFieldsInfoVO;
import jnpf.base.model.comfields.ComFieldsListVO;
import jnpf.base.model.comfields.ComFieldsUpForm;
import jnpf.util.JsonUtil;
import jnpf.util.JsonUtilEx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Api(tags = "常用字段", value = "CommonFields")
@RestController
@RequestMapping("/api/system/CommonFields")
public class ComFieldsController {

    @Autowired
    private ComFieldsService comFieldsService;

    @GetMapping
    public ActionResult<ListVO<ComFieldsListVO>> list() {
        List<ComFieldsEntity> data = comFieldsService.getList();
        List<ComFieldsListVO> list= JsonUtil.getJsonToList(data,ComFieldsListVO.class);
        ListVO<ComFieldsListVO> vo=new ListVO<>();
        vo.setList(list);
        return ActionResult.success(vo);
    }

    @GetMapping("/{id}")
    public ActionResult<ComFieldsInfoVO> info(@PathVariable("id") String id) throws DataException {
        ComFieldsEntity entity = comFieldsService.getInfo(id);
        ComFieldsInfoVO vo= JsonUtilEx.getJsonToBeanEx(entity,ComFieldsInfoVO.class);
        return ActionResult.success(vo);
    }

    @PostMapping
    public ActionResult create(@RequestBody @Valid ComFieldsCrForm comFieldsCrForm) {
        ComFieldsEntity entity = JsonUtil.getJsonToBean(comFieldsCrForm, ComFieldsEntity.class);
        if (comFieldsService.isExistByFullName(entity.getField(),entity.getId())){
            return ActionResult.fail("名称不能重复");
        }
        comFieldsService.create(entity);
        return ActionResult.success("新建成功");
    }

    @PutMapping("/{id}")
    public ActionResult update(@PathVariable("id") String id, @RequestBody @Valid ComFieldsUpForm comFieldsUpForm) {
        ComFieldsEntity entity = JsonUtil.getJsonToBean(comFieldsUpForm, ComFieldsEntity.class);
        if (comFieldsService.isExistByFullName(entity.getField(),id)){
            return ActionResult.fail("名称不能重复");
        }
        boolean flag = comFieldsService.update(id, entity);
        if (flag==false){
            return ActionResult.fail("更新失败，数据不存在");
        }
        return ActionResult.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public ActionResult delete(@PathVariable("id") String id) {
        ComFieldsEntity entity = comFieldsService.getInfo(id);
        if (entity != null) {
            comFieldsService.delete(entity);
            return ActionResult.success("删除成功");
        }
        return ActionResult.fail("删除失败,数据不存在");
    }
}

