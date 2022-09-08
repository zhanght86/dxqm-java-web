package jnpf.base.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.ActionResult;
import jnpf.base.Page;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.service.DblinkService;
import jnpf.base.service.DictionaryDataService;
import jnpf.base.vo.ListVO;
import jnpf.database.model.DbLinkEntity;
import jnpf.database.exception.DataException;
import jnpf.base.model.dblink.*;
import jnpf.permission.model.user.UserAllModel;
import jnpf.permission.service.UserService;
import jnpf.util.JsonUtil;
import jnpf.util.JsonUtilEx;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.newtreeutil.TreeDotUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据连接
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Api(tags = "数据连接", value = "DataSource")
@RestController
@RequestMapping("/api/system/DataSource")
public class DbLinkController {

    @Autowired
    private DblinkService dblinkService;
    @Autowired
    private UserService userService;
    @Autowired
    private DictionaryDataService dictionaryDataService;

    /**
     * 列表
     *
     * @return
     */
    @GetMapping("/Selector")
    @ApiOperation("获取数据连接下拉框列表")
    public ActionResult<ListVO<DbLinkListVO>> selectorList() {
        List<DbLinkEntity> data = dblinkService.getList();
        List<String> dbType = data.stream().map(t -> t.getDbType()).collect(Collectors.toList());
        List<DictionaryDataEntity> typeList = dictionaryDataService.getDictionName(dbType);
        List<DbLinkModel> modelAll = new LinkedList<>();
        for (DictionaryDataEntity entity : typeList) {
            DbLinkModel model = new DbLinkModel();
            model.setFullName(entity.getFullName());
            model.setId(entity.getId());
            Long num = data.stream().filter(t -> t.getDbType().equals(entity.getEnCode())).count();
            model.setNum(num);
            modelAll.add(model);
        }
        for (DbLinkEntity entity : data) {
            DbLinkModel model = JsonUtil.getJsonToBean(entity, DbLinkModel.class);
            DictionaryDataEntity dataEntity = typeList.stream().filter(t -> t.getEnCode().equals(entity.getDbType())).findFirst().orElse(null);
            if (dataEntity != null) {
                model.setParentId(dataEntity.getId());
                modelAll.add(model);
            }
        }
        List<SumTree<DbLinkModel>> trees = TreeDotUtils.convertListToTreeDot(modelAll);
        List<DbLinkListVO> list = JsonUtil.getJsonToList(trees, DbLinkListVO.class);
        ListVO vo = new ListVO();
        vo.setList(list);
        return ActionResult.success(vo);
    }


    /**
     * 列表
     *
     * @param page 关键字
     * @return
     */
    @GetMapping
    @ApiOperation("获取数据连接列表")
    public ActionResult<ListVO<DbLinkListVO>> getList(Page page) {
        List<UserAllModel> userAllVos = userService.getAll();
        List<DbLinkEntity> data = dblinkService.getList(page.getKeyword());
        List<String> dbType = data.stream().map(t -> t.getDbType()).collect(Collectors.toList());
        List<DictionaryDataEntity> typeList = dictionaryDataService.getDictionName(dbType);
        List<DbLinkModel> modelAll = new LinkedList<>();
        Long enabledNum = 0L;
        for (DictionaryDataEntity entity : typeList) {
            DbLinkModel model = new DbLinkModel();
            model.setFullName(entity.getFullName());
            model.setId(entity.getId());
            Long num = data.stream().filter(t -> t.getDbType().equals(entity.getEnCode())).count();
            enabledNum += num;
            model.setNum(num);
            modelAll.add(model);
        }
        //判断失去字典的连接
        Long otherNum = data.size()- enabledNum;
        if(otherNum != 0){
            DbLinkModel modelTemp = new DbLinkModel();
            modelTemp.setFullName("=== 失去字典 ===");
            modelTemp.setId("other");
            modelTemp.setNum(otherNum);
            modelAll.add(modelTemp);
        }
        for (DbLinkEntity entity : data) {
            DbLinkModel model = JsonUtil.getJsonToBean(entity, DbLinkModel.class);
            //存在类型的字典对象
            DictionaryDataEntity dataEntity = typeList.stream().filter(t -> t.getEnCode().equals(entity.getDbType())).findFirst().orElse(null);
            String parentId = null;
            if (dataEntity != null ) {
                parentId = dataEntity.getId();
            }else {
                if(otherNum != 0){
                    parentId = "other";
                }
            }
            model.setParentId(parentId);
            //创建者
            UserAllModel creatorUser = userAllVos.stream().filter(t -> t.getId().equals(entity.getCreatorUserId())).findFirst().orElse(null);
            model.setCreatorUser(creatorUser != null ? creatorUser.getRealName() + "/" + creatorUser.getAccount() : entity.getCreatorUserId());
            //修改人
            UserAllModel lastModifyUser = userAllVos.stream().filter(t -> t.getId().equals(entity.getLastModifyUserId())).findFirst().orElse(null);
            model.setLastModifyUser(lastModifyUser != null ? lastModifyUser.getRealName() + "/" + lastModifyUser.getAccount() : entity.getLastModifyUserId());
            modelAll.add(model);


        }
        List<SumTree<DbLinkModel>> trees = TreeDotUtils.convertListToTreeDot(modelAll);
        List<DbLinkListVO> list = JsonUtil.getJsonToList(trees, DbLinkListVO.class);
        ListVO vo = new ListVO();
        vo.setList(list);
        return ActionResult.success(vo);
    }

    /**
     * 信息
     *
     * @param id 主键
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("获取数据连接")
    public ActionResult<DbLinkInfoVO> get(@PathVariable("id") String id) throws DataException {
        DbLinkEntity entity = dblinkService.getInfo(id);
        DbLinkInfoVO vo = JsonUtilEx.getJsonToBeanEx(entity, DbLinkInfoVO.class);
        return ActionResult.success(vo);
    }

    /**
     * 新建
     *
     * @param dbLinkCrForm dto实体
     * @return
     */
    @PostMapping
    @ApiOperation("添加数据连接")
    public ActionResult create(@RequestBody @Valid DbLinkCrForm dbLinkCrForm) {
        DbLinkEntity entity = JsonUtil.getJsonToBean(dbLinkCrForm, DbLinkEntity.class);
        //TODO dblink里面添加两个数据：模式，表空间
        if (dblinkService.isExistByFullName(entity.getFullName(), entity.getId())) {
            return ActionResult.fail("名称不能重复");
        }
        dblinkService.create(entity);
        return ActionResult.success("创建成功");
    }

    /**
     * 更新
     *
     * @param id           主键
     * @param dbLinkUpForm dto实体
     * @return
     */
    @PutMapping("/{id}")
    @ApiOperation("修改数据连接")
    public ActionResult update(@PathVariable("id") String id, @RequestBody @Valid DbLinkUpForm dbLinkUpForm) {
        DbLinkEntity entity = JsonUtil.getJsonToBean(dbLinkUpForm, DbLinkEntity.class);
        if (dblinkService.isExistByFullName(entity.getFullName(), id)) {
            return ActionResult.fail("名称不能重复");
        }
        boolean flag = dblinkService.update(id, entity);
        if (flag == false) {
            return ActionResult.fail("更新失败，数据不存在");
        }
        return ActionResult.success("更新成功");
    }

    /**
     * 删除
     *
     * @param id 主键
     * @return
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除数据连接")
    public ActionResult delete(@PathVariable("id") String id) {
        DbLinkEntity entity = dblinkService.getInfo(id);
        if (entity != null) {
            dblinkService.delete(entity);
            return ActionResult.success("删除成功");
        }
        return ActionResult.fail("删除失败，数据不存在");
    }

    @PostMapping("/Actions/Test")
    @ApiOperation("测试连接")
    public ActionResult test(@RequestBody DbLinkTestForm dbLinkTestForm) throws DataException{
        DbLinkEntity entity = JsonUtil.getJsonToBean(dbLinkTestForm, DbLinkEntity.class);
        boolean data = dblinkService.testDbConnection(entity);
        if (data) {
            return ActionResult.success("连接成功");
        } else {
            return ActionResult.fail("连接失败");
        }
    }
}
