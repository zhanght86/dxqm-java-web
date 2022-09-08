package jnpf.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.message.entity.SynThirdInfoEntity;
import jnpf.message.mapper.SynThirdInfoMapper;
import jnpf.message.service.SynThirdInfoService;
import jnpf.message.util.SynThirdConsts;
import jnpf.message.util.SynThirdTotal;
import jnpf.permission.service.OrganizeService;
import jnpf.permission.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * 第三方工具的公司-部门-用户同步表模型
 *
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021/4/23 17:33
 */
@Service
public class SynThirdInfoServiceImpl extends ServiceImpl<SynThirdInfoMapper, SynThirdInfoEntity> implements SynThirdInfoService {

    @Autowired
    private OrganizeService organizeServicel;
    @Autowired
    private UserService userService;

    @Override
    public List<SynThirdInfoEntity> getList(String thirdType, String dataType) {
        QueryWrapper<SynThirdInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().and(t->t.eq(SynThirdInfoEntity::getThirdtype,thirdType));
        queryWrapper.lambda().and(t->t.eq(SynThirdInfoEntity::getDatatype,dataType));
        return this.list(queryWrapper);
    }

    @Override
    public SynThirdInfoEntity getInfo(String id) {
        QueryWrapper<SynThirdInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SynThirdInfoEntity::getId,id);
        return this.getOne(queryWrapper);
    }

    @Override
    public void create(SynThirdInfoEntity entity) {
        this.save(entity);
    }

    @Override
    public boolean update(String id, SynThirdInfoEntity entity) {
        entity.setId(id);
        return this.updateById(entity);
    }

    @Override
    public void delete(SynThirdInfoEntity entity) {
        if(entity!=null){
            this.removeById(entity.getId());
        }
    }

    @Override
    public SynThirdInfoEntity getInfoBySysObjId(String thirdType,String dataType,String id) {
        QueryWrapper<SynThirdInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().and(t->t.eq(SynThirdInfoEntity::getThirdtype,thirdType));
        queryWrapper.lambda().and(t->t.eq(SynThirdInfoEntity::getDatatype,dataType));
        queryWrapper.lambda().and(t->t.eq(SynThirdInfoEntity::getSysObjId,id));
        return this.getOne(queryWrapper);
    }

    @Override
    public SynThirdTotal getSynTotal(String thirdType, String dataType) {
        String synType = dataType.equals(SynThirdConsts.DATA_TYPE_ORG) ? "组织" : "用户";
        Integer recordTotal = 0;
        Long synSuccessCount = 0L;
        Long synFailCount = 0L;
        Long unSynCount = 0L;
        Date synDate = null;

        // 获取列表数据
        List<SynThirdInfoEntity> synList = getList(thirdType, dataType);
        if(synList!=null && synList.size()>0){
            synSuccessCount = synList.stream().filter(t -> t.getSynstate().equals(SynThirdConsts.SYN_STATE_OK)).count();
            synFailCount = synList.stream().filter(t -> t.getSynstate().equals(SynThirdConsts.SYN_STATE_FAIL)).count();
            unSynCount = synList.stream().filter(t -> t.getSynstate().equals(SynThirdConsts.SYN_STATE_NO)).count();
            synDate = synList.stream().max(Comparator.comparing(u -> u.getLastModifyTime())).get().getLastModifyTime();
        }

        // 获取本系统的组织、用户表的记录数
        if(dataType.equals(SynThirdConsts.DATA_TYPE_ORG)){
            // 获取组织(公司和部门)的记录数
            recordTotal = organizeServicel.getList().size();
        }else{
            // 获取用户的记录数
            recordTotal = userService.getList().size();
        }

        // 写入同步统计模型对象
        SynThirdTotal synThirdTotal = new SynThirdTotal();
        synThirdTotal.setSynType(synType);
        synThirdTotal.setRecordTotal(recordTotal);
        synThirdTotal.setSynSuccessCount(synSuccessCount);
        synThirdTotal.setSynFailCount(synFailCount);
        synThirdTotal.setUnSynCount(unSynCount);
        synThirdTotal.setSynDate(synDate);

        return synThirdTotal;
    }

}
