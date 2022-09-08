package jnpf.portal.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.entity.VisualdevEntity;
import jnpf.base.vo.DownloadVO;
import jnpf.config.ConfigValueUtil;
import jnpf.onlinedev.model.BaseDevModelVO;
import jnpf.portal.model.*;
import jnpf.util.*;
import jnpf.base.ActionResult;
import jnpf.base.UserInfo;
import jnpf.base.service.DictionaryDataService;
import jnpf.base.vo.ListVO;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.permission.entity.AuthorizeEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.portal.entity.PortalEntity;
import jnpf.permission.service.AuthorizeService;
import jnpf.permission.service.UserService;
import jnpf.portal.service.PortalService;
import jnpf.util.enums.ExportModelTypeEnum;
import jnpf.util.file.FileExport;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.newtreeutil.TreeDotUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 可视化门户
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Slf4j
@RestController
@Api(tags = "可视化门户", value = "Portal")
@RequestMapping("/api/visualdev/Portal")
public class PortalController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private PortalService portalService;
    @Autowired
    private AuthorizeService authorizeService;
    @Autowired
    private DictionaryDataService dictionaryDataService;
    @Autowired
    private CacheKeyUtil cacheKeyUtil;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private FileExport fileExport;
    @Autowired
    private ConfigValueUtil configValueUtil;


    @GetMapping
    public ActionResult list(PortalPagination portalPagination) {
        List<PortalEntity> data = portalService.getList(portalPagination);
        List<DictionaryDataEntity> dictionList = dictionaryDataService.getList();
        List<UserEntity> userList = userService.getList();
        List<String> datalist = data.stream().map(t -> t.getCategory()).distinct().collect(Collectors.toList());
        List<PortalTreeModel> modelAll = new LinkedList<>();
        for (String id : datalist) {
            DictionaryDataEntity dataEntity = dictionList.stream().filter(t->t.getId().equals(id)).findFirst().orElse(null);
            if(dataEntity!=null){
                PortalTreeModel model = new PortalTreeModel();
                model.setFullName(dataEntity.getFullName());
                model.setId(dataEntity.getId());
                long num = data.stream().filter(t -> t.getCategory().equals(id)).count();
                model.setNum(num);
                if (num > 0) {
                    modelAll.add(model);
                }
            }
        }
        for (PortalEntity entity : data) {
            PortalTreeModel model = JsonUtil.getJsonToBean(entity, PortalTreeModel.class);
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
        List<SumTree<PortalTreeModel>> trees = TreeDotUtils.convertListToTreeDot(modelAll);
        List<PortalListVO> list = JsonUtil.getJsonToList(trees, PortalListVO.class);
        ListVO vo = new ListVO();
        vo.setList(list);
        return ActionResult.success(vo);
    }

    @GetMapping("/Selector")
    public ActionResult listSelcet(String type) {
        List<PortalSelectModel> modelList = portalService.getList(type);
        List<SumTree<PortalSelectModel>> sumTrees = TreeDotUtils.convertListToTreeDot(modelList);
        List<PortalSelectVO> listVO = JsonUtil.getJsonToList(sumTrees, PortalSelectVO.class);
        ListVO<PortalSelectVO> treeVo = new ListVO<>();
        treeVo.setList(listVO);
        return ActionResult.success(treeVo);
    }

    @GetMapping("/{id}")
    public ActionResult<PortalInfoVO> info(@PathVariable("id") String id) {
        PortalEntity entity = portalService.getInfo(id);
        PortalInfoVO vo = JsonUtil.getJsonToBean(JsonUtilEx.getObjectToStringDateFormat(entity, "yyyy-MM-dd HH:mm:ss"), PortalInfoVO.class);

        return ActionResult.success(vo);
    }

    @GetMapping("/{id}/auth")
    public ActionResult<PortalInfoVO> infoAuth(@PathVariable("id") String id) {
        UserInfo userInfo=userProvider.get();
        if((userInfo!=null&&userInfo.getRoleIds()!=null)){
            for(String roleId:userInfo.getRoleIds()){
                List<AuthorizeEntity> authorizeEntityList=authorizeService.getListByObjectId(roleId).stream().filter(t->"portal".equals(t.getItemType())).collect(Collectors.toList());
                for(AuthorizeEntity authorizeEntity:authorizeEntityList){
                    if(id.equals( authorizeEntity.getItemId())){
                        PortalEntity entity = portalService.getInfo(id);
                        PortalInfoAuthVO vo = JsonUtil.getJsonToBean(JsonUtilEx.getObjectToStringDateFormat(entity, "yyyy-MM-dd HH:mm:ss"), PortalInfoAuthVO.class);
                        return ActionResult.success(vo);
                    }
                }
            }
        }
        if(userInfo.getIsAdministrator()==true){
            PortalEntity entity = portalService.getInfo(id);
            PortalInfoAuthVO vo = JsonUtil.getJsonToBean(JsonUtilEx.getObjectToStringDateFormat(entity, "yyyy-MM-dd HH:mm:ss"), PortalInfoAuthVO.class);
            return ActionResult.success(vo);
        }
        return ActionResult.fail("您没有此门户使用权限，请重新设置");
    }


    @DeleteMapping("/{id}")
    @Transactional
    public ActionResult delete(@PathVariable("id") String id) {
        PortalEntity entity = portalService.getInfo(id);
        if (entity != null) {
            portalService.delete(entity);
            QueryWrapper<AuthorizeEntity> queryWrapper=new QueryWrapper();
            queryWrapper.lambda().eq(AuthorizeEntity::getItemId,id);
            authorizeService.remove(queryWrapper);
        }
        return ActionResult.success("删除成功");
    }

    @PostMapping()
    @Transactional
    public ActionResult create(@RequestBody @Valid PortalCrForm portalCrForm) {
        PortalEntity entity = JsonUtil.getJsonToBean(portalCrForm, PortalEntity.class);
        entity.setId(RandomUtil.uuId());
        portalService.create(entity);
        return ActionResult.success("新建成功");
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
        PortalEntity entity = portalService.getInfo(id);
        entity.setEnabledMark(0);
        entity.setFullName(entity.getFullName() + "_副本");
        entity.setLastModifyTime(null);
        entity.setLastModifyUser(null);
        entity.setId(RandomUtil.uuId());
        PortalEntity entity1 = JsonUtil.getJsonToBean(entity, PortalEntity.class);
        portalService.create(entity1);
        return ActionResult.success("复制成功");
    }


    @PutMapping("/{id}")
    @Transactional
    public ActionResult update(@PathVariable("id") String id, @RequestBody @Valid PortalUpForm portalUpForm) {
        PortalEntity entity = JsonUtil.getJsonToBean(portalUpForm, PortalEntity.class);
        boolean flag = portalService.update(id, entity);
        if (flag == false) {
            return ActionResult.fail("更新失败，数据不存在");
        }
        return ActionResult.success("更新成功");

    }





    /**
     * 门户权限列表
     *
     * @param id 对象主键
     * @return
     */
    @Transactional
    @ApiOperation("设置默认门户")
    @PutMapping("/{id}/Actions/SetDefault")
    public ActionResult setDefault(@PathVariable("id") String id) {
        UserEntity userEntity=userService.getInfo(userProvider.get().getUserId());
        if(userEntity!=null){
            userEntity.setPortalId(id);
            userService.updateById(userEntity);
            String catchKey = cacheKeyUtil.getAllUser();
            if (redisUtil.exists(catchKey)) {
                redisUtil.remove(catchKey);
            }
        }else{
            return ActionResult.fail("设置失败，用户不存在");
        }
        return ActionResult.success("设置成功");
    }

    @ApiOperation("门户导出")
    @PostMapping("/{modelId}/Actions/ExportData")
    public ActionResult exportFunction(@PathVariable("modelId") String modelId){
        PortalEntity entity = portalService.getInfo(modelId);
        if (entity!=null){
            PortalExportDataVo vo =new PortalExportDataVo();
            BeanUtils.copyProperties(entity,vo);
            vo.setModelType(ExportModelTypeEnum.Portal.getMessage());
            DownloadVO downloadVO=fileExport.exportFile(vo,configValueUtil.getTemporaryFilePath());
            return ActionResult.success(downloadVO);
        }else{
            return ActionResult.success("并无该条数据");
        }
    }

    @ApiOperation("门户导入")
    @PostMapping(value = "/Model/Actions/ImportData", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ActionResult importFunction(@RequestPart("file") MultipartFile multipartFile){
        //判断是否为.json结尾
        if (FileUtil.existsSuffix(multipartFile)){
            return ActionResult.fail("导入文件格式错误");
        }
        //获取文件内容
        String fileContent = FileUtil.getFileContent(multipartFile, configValueUtil.getTemporaryFilePath());
        PortalExportDataVo vo = JsonUtil.getJsonToBean(fileContent,PortalExportDataVo.class);
        if (vo.getModelType()==null||!vo.getModelType().equals(ExportModelTypeEnum.Portal.getMessage())){
            return ActionResult.fail("请导入对应功能的json文件");
        }
        String modelId =vo.getId();
        if (StringUtil.isNotEmpty(modelId)){
            PortalEntity portalEntity = portalService.getInfo(modelId);
            if (portalEntity!=null){
                return ActionResult.fail("已存在相同功能");
            }
        }
        PortalEntity entity=JsonUtil.getJsonToBean(fileContent,PortalEntity.class);
        portalService.create(entity);
        return ActionResult.success("导入成功");
    }
}
