package jnpf.base.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.ActionResult;
import jnpf.base.model.*;
import jnpf.base.service.DictionaryDataService;
import jnpf.base.service.VisualdevService;
import jnpf.exception.WorkFlowException;
import jnpf.model.visiual.FormDataField;
import jnpf.model.visiual.JnpfKeyConsts;
import jnpf.model.visiual.VisualdevTreeChildModel;
import jnpf.permission.entity.UserEntity;
import jnpf.util.JsonUtilEx;
import jnpf.base.vo.ListVO;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.entity.VisualdevEntity;
import jnpf.database.exception.DataException;
import jnpf.base.model.template6.BtnData;
import jnpf.onlinedev.model.PaginationModel;
import jnpf.model.visiual.fields.FieLdsModel;
import jnpf.onlinedev.service.VisualdevModelDataService;
import jnpf.permission.service.UserService;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import jnpf.base.util.VisualUtil;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.newtreeutil.TreeDotUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 可视化基础模块
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Api(tags = "可视化基础模块", value = "Base")
@RestController
@RequestMapping("/api/visualdev/Base")
public class VisualdevController {

    @Autowired
    private VisualdevService visualdevService;
    @Autowired
    private UserService userService;
    @Autowired
    private DictionaryDataService dictionaryDataService;
    @Autowired
    private VisualdevModelDataService visualdevModelDataService;


    @ApiOperation("获取功能列表")
    @GetMapping
    public ActionResult list(PaginationVisualdev paginationVisualdev) {
        List<VisualdevEntity> data = visualdevService.getList(paginationVisualdev);
        List<DictionaryDataEntity> dictionList = dictionaryDataService.getList();
        List<UserEntity> userList = userService.getList();
        List<String> datalist = data.stream().map(t -> t.getCategory()).distinct().collect(Collectors.toList());
        List<VisualTreeModel> modelAll = new LinkedList<>();
        for (String id : datalist) {
            DictionaryDataEntity dataEntity = dictionList.stream().filter(t->t.getId().equals(id)).findFirst().orElse(null);
            if(dataEntity!=null){
                VisualTreeModel model = new VisualTreeModel();
                model.setFullName(dataEntity.getFullName());
                model.setId(dataEntity.getId());
                long num = data.stream().filter(t -> t.getCategory().equals(id)).count();
                model.setNum(num);
                if (num > 0) {
                    modelAll.add(model);
                }
            }
        }
        for (VisualdevEntity entity : data) {
            VisualTreeModel model = JsonUtil.getJsonToBean(entity, VisualTreeModel.class);
            DictionaryDataEntity dataEntity = dictionList.stream().filter(t -> t.getId().equals(entity.getCategory())).findFirst().orElse(null);
            if (dataEntity != null) {
                model.setParentId(dataEntity.getId());
                UserEntity creatorUser = userList.stream().filter(t -> t.getId().equals(model.getCreatorUser())).findFirst().orElse(null);
                if (creatorUser != null) {
                    model.setCreatorUser(creatorUser.getRealName() + "/" + creatorUser.getAccount());
                }
                UserEntity lastmodifyuser = userList.stream().filter(t -> t.getId().equals(model.getLastModifyUser())).findFirst().orElse(null);
                if (lastmodifyuser != null) {
                    model.setLastModifyUser(lastmodifyuser.getRealName() + "/" + lastmodifyuser.getAccount());
                }
                modelAll.add(model);
            }
        }
        List<SumTree<VisualTreeModel>> trees = TreeDotUtils.convertListToTreeDot(modelAll);
        List<VisualDevListVO> list = JsonUtil.getJsonToList(trees, VisualDevListVO.class);
        ListVO vo = new ListVO();
        vo.setList(list);
        return ActionResult.success(vo);
    }

    @ApiOperation("获取功能列表下拉框")
    @GetMapping("/Selector")
    public ActionResult selectorList(Integer type) {
        List<VisualdevEntity> list = visualdevService.getList().stream().filter(t -> t.getState() == 1).collect(Collectors.toList());
        if (type != null) {
            list = list.stream().filter(t -> type.equals(t.getType())).collect(Collectors.toList());
        }
        List<VisualdevTreeVO> voList = new ArrayList<>();
        HashSet<String> cate = new HashSet<>(16);
        for (VisualdevEntity entity : list) {
            if (!StringUtil.isEmpty(entity.getCategory())) {
                DictionaryDataEntity dataEntity = dictionaryDataService.getInfo(entity.getCategory());
                if (dataEntity != null) {
                    int i = cate.size();
                    cate.add(dataEntity.getId());
                    if (cate.size() == i + 1) {
                        VisualdevTreeVO visualdevTreeVO = new VisualdevTreeVO();
                        visualdevTreeVO.setId(entity.getCategory());
                        visualdevTreeVO.setFullName(dataEntity.getFullName());
                        visualdevTreeVO.setHasChildren(true);
                        voList.add(visualdevTreeVO);
                    }
                }
            }
        }

        for (VisualdevTreeVO vo : voList) {
            List<VisualdevTreeChildModel> visualdevTreeChildModelList = new ArrayList<>();
            for (VisualdevEntity entity : list) {
                if (vo.getId().equals(entity.getCategory())) {
                    VisualdevTreeChildModel model = JsonUtil.getJsonToBean(entity, VisualdevTreeChildModel.class);
                    visualdevTreeChildModelList.add(model);
                }
            }
            vo.setChildren(visualdevTreeChildModelList);
        }
        ListVO listVO = new ListVO();
        listVO.setList(voList);
        return ActionResult.success(listVO);
    }

    @ApiOperation("获取功能信息")
    @GetMapping("/{id}")
    public ActionResult info(@PathVariable("id") String id) throws DataException {
        VisualdevEntity entity = visualdevService.getInfo(id);
        VisualDevInfoVO vo = JsonUtilEx.getJsonToBeanEx(entity, VisualDevInfoVO.class);
        return ActionResult.success(vo);
    }

    @ApiOperation("获取表单主表属性下拉框")
    @GetMapping("/{id}/FormDataFields")
    public ActionResult getFormData(@PathVariable("id") String id) {
        VisualdevEntity entity = visualdevService.getInfo(id);
        Map<String, Object> formData = JsonUtil.stringToMap(entity.getFormData());
        List<FieLdsModel> fieLdsModelList = JsonUtil.getJsonToList(formData.get("fields").toString(), FieLdsModel.class);
        List<FormDataField> formDataFieldList = new ArrayList<>();
        for (FieLdsModel fieLdsModel : fieLdsModelList) {
            if (!"".equals(fieLdsModel.getVModel()) && !JnpfKeyConsts.CHILD_TABLE.equals(fieLdsModel.getConfig().getJnpfKey()) && !"relationForm".equals(fieLdsModel.getConfig().getJnpfKey())) {
                FormDataField formDataField = new FormDataField();
                formDataField.setLabel(fieLdsModel.getConfig().getLabel());
                formDataField.setVModel(fieLdsModel.getVModel());
                formDataFieldList.add(formDataField);
            }
        }
        ListVO<FormDataField> listVO = new ListVO();
        listVO.setList(formDataFieldList);
        return ActionResult.success(listVO);
    }

    @ApiOperation("获取表单主表属性列表")
    @GetMapping("/{id}/FieldDataSelect")
    public ActionResult getFormData(@PathVariable("id") String id, String key) throws ParseException, DataException, IOException, SQLException {
        VisualdevEntity visualdevEntity = visualdevService.getInfo(id);
        List<Map<String, Object>> realList = visualdevModelDataService.getListResultAll(visualdevEntity);
        List<Map<String, Object>> dataAll = new ArrayList<>();
        if (StringUtil.isNotEmpty(key)) {
            for (Map<String, Object> map : realList) {
                for (Object value : map.values()) {
                    if (String.valueOf(value).contains(key)) {
                        dataAll.add(map);
                        break;
                    }
                }
            }
        } else {
            dataAll = realList;
        }
        return ActionResult.success(dataAll);
    }


    /**
     * 复制功能
     *
     * @param id
     * @return
     */
    @ApiOperation("复制功能")
    @PostMapping("/{id}/Actions/Copy")
    public ActionResult copyInfo(@PathVariable("id") String id) {
        VisualdevEntity entity = visualdevService.getInfo(id);
        entity.setState(0);
        entity.setFullName(entity.getFullName() + "_副本");
        entity.setLastModifyTime(null);
        entity.setLastModifyUser(null);
        entity.setCreatorTime(null);
        entity.setId(RandomUtil.uuId());
        VisualdevEntity entity1 = JsonUtil.getJsonToBean(entity, VisualdevEntity.class);
        visualdevService.create(entity1);
        return ActionResult.success("复制成功");
    }


    /**
     * 更新功能状态
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("更新功能状态")
    @PutMapping("/{id}/Actions/State")
    public ActionResult update(@PathVariable("id") String id) {
        VisualdevEntity entity = visualdevService.getInfo(id);
        if (entity != null) {
            if (entity.getState() == 1) {
                entity.setState(0);
            } else {
                entity.setState(1);
            }
            boolean flag = visualdevService.update(entity.getId(), entity);
            if (flag == false) {
                return ActionResult.fail("更新失败，任务不存在");
            }
        }
        return ActionResult.success("更新成功");
    }


    @ApiOperation("新建功能")
    @PostMapping
    public ActionResult create(@RequestBody VisualDevCrForm visualDevCrForm) {
        VisualdevEntity entity = JsonUtil.getJsonToBean(JsonUtilEx.getObjectToString(visualDevCrForm), VisualdevEntity.class);
        visualdevService.create(entity);
        return ActionResult.success("新建成功");
    }


    @ApiOperation("修改功能")
    @PutMapping("/{id}")
    public ActionResult update(@PathVariable("id") String id, @RequestBody VisualDevUpForm visualDevUpForm) {
        VisualdevEntity entity = JsonUtil.getJsonToBean(JsonUtilEx.getObjectToString(visualDevUpForm), VisualdevEntity.class);
        boolean flag = visualdevService.update(id, entity);
        if (flag == false) {
            return ActionResult.fail("更新失败，数据不存在");
        }
        return ActionResult.success("更新成功");
    }


    @ApiOperation("删除功能")
    @DeleteMapping("/{id}")
    public ActionResult delete(@PathVariable("id") String id) throws WorkFlowException {
        VisualdevEntity entity = visualdevService.getInfo(id);
        if (entity != null) {
            visualdevService.delete(entity);
            return ActionResult.success("删除成功");
        }
        return ActionResult.fail("删除失败，数据不存在");
    }

    @ApiOperation("获取模板按钮和列表字段")
    @GetMapping("/ModuleBtn")
    public ActionResult getModuleBtn(String moduleId) {
        VisualdevEntity visualdevEntity =visualdevService.getInfo(moduleId);
        //去除模板中的F_
        VisualUtil.delfKey(visualdevEntity);
        List<BtnData> btnData =new ArrayList<>();
        Map<String,Object> column=JsonUtil.stringToMap(visualdevEntity.getColumnData());
        if(column.get("columnBtnsList")!=null){
            btnData.addAll(JsonUtil.getJsonToList(JsonUtil.getJsonToListMap(column.get("columnBtnsList").toString()),BtnData.class));
        }
        if(column.get("btnsList")!=null){
            btnData.addAll(JsonUtil.getJsonToList(JsonUtil.getJsonToListMap(column.get("btnsList").toString()),BtnData.class));
        }
        return ActionResult.success(btnData);
    }

}
