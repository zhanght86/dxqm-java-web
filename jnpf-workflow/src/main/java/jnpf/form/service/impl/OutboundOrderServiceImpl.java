package jnpf.form.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.base.service.BillRuleService;
import jnpf.engine.service.FlowTaskService;
import jnpf.exception.WorkFlowException;
import jnpf.form.entity.OutboundEntryEntity;
import jnpf.form.entity.OutboundOrderEntity;
import jnpf.form.mapper.OutboundOrderMapper;
import jnpf.form.model.outboundorder.OutboundEntryEntityInfoModel;
import jnpf.form.model.outboundorder.OutboundOrderForm;
import jnpf.form.service.OutboundEntryService;
import jnpf.form.service.OutboundOrderService;
import jnpf.engine.util.ModelUtil;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 出库单
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月29日 上午9:18
 */
@Service
public class OutboundOrderServiceImpl extends ServiceImpl<OutboundOrderMapper, OutboundOrderEntity> implements OutboundOrderService {

    @Autowired
    private BillRuleService billRuleService;
    @Autowired
    private OutboundEntryService outboundEntryService;
    @Autowired
    private FlowTaskService flowTaskService;

    @Override
    public List<OutboundEntryEntity> getOutboundEntryList(String id) {
        QueryWrapper<OutboundEntryEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OutboundEntryEntity::getOutboundId, id).orderByDesc(OutboundEntryEntity::getSortCode);
        return outboundEntryService.list(queryWrapper);
    }

    @Override
    public OutboundOrderEntity getInfo(String id) {
        QueryWrapper<OutboundOrderEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OutboundOrderEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = WorkFlowException.class)
    public void save(String id, OutboundOrderEntity entity, List<OutboundEntryEntity> outboundEntryEntityList) throws WorkFlowException {
        //表单信息
        if (id == null) {
            entity.setId(RandomUtil.uuId());
            for (int i = 0; i < outboundEntryEntityList.size(); i++) {
                outboundEntryEntityList.get(i).setId(RandomUtil.uuId());
                outboundEntryEntityList.get(i).setOutboundId(entity.getId());
                outboundEntryEntityList.get(i).setSortCode(Long.parseLong(i + ""));
                outboundEntryService.save(outboundEntryEntityList.get(i));
            }
            //创建
            this.save(entity);
            billRuleService.useBillNumber("WF_OutboundOrderNo");
        } else {
            entity.setId(id);
            QueryWrapper<OutboundEntryEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(OutboundEntryEntity::getOutboundId, entity.getId());
            outboundEntryService.remove(queryWrapper);
            for (int i = 0; i < outboundEntryEntityList.size(); i++) {
                outboundEntryEntityList.get(i).setId(RandomUtil.uuId());
                outboundEntryEntityList.get(i).setOutboundId(entity.getId());
                outboundEntryEntityList.get(i).setSortCode(Long.parseLong(i + ""));
                outboundEntryService.save(outboundEntryEntityList.get(i));
            }
            //编辑
            this.updateById(entity);
        }
        //流程信息
        ModelUtil.save(id, entity.getFlowId(), entity.getId(), entity.getFlowTitle(), entity.getFlowUrgent(), entity.getBillNo(), entity);
    }

    @Override
    @Transactional(rollbackFor = WorkFlowException.class)
    public void submit(String id, OutboundOrderEntity entity, List<OutboundEntryEntity> outboundEntryEntityList) throws WorkFlowException {
        //表单信息
        if (id == null) {
            entity.setId(RandomUtil.uuId());
            for (int i = 0; i < outboundEntryEntityList.size(); i++) {
                outboundEntryEntityList.get(i).setId(RandomUtil.uuId());
                outboundEntryEntityList.get(i).setOutboundId(entity.getId());
                outboundEntryEntityList.get(i).setSortCode(Long.parseLong(i + ""));
                outboundEntryService.save(outboundEntryEntityList.get(i));
            }
            //创建
            save(entity);
            billRuleService.useBillNumber("WF_OutboundOrderNo");
        } else {
            entity.setId(id);
            QueryWrapper<OutboundEntryEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(OutboundEntryEntity::getOutboundId, entity.getId());
            outboundEntryService.remove(queryWrapper);
            for (int i = 0; i < outboundEntryEntityList.size(); i++) {
                outboundEntryEntityList.get(i).setId(RandomUtil.uuId());
                outboundEntryEntityList.get(i).setOutboundId(entity.getId());
                outboundEntryEntityList.get(i).setSortCode(Long.parseLong(i + ""));
                outboundEntryService.save(outboundEntryEntityList.get(i));
            }
            //编辑
            this.updateById(entity);
        }
        //流程信息
        ModelUtil.submit(id, entity.getFlowId(), entity.getId(), entity.getFlowTitle(), entity.getFlowUrgent(), entity.getBillNo(), entity, null);
    }

    @Override
    public void data(String id, String data) {
        OutboundOrderForm outboundOrderForm = JsonUtil.getJsonToBean(data, OutboundOrderForm.class);
        OutboundOrderEntity entity = JsonUtil.getJsonToBean(outboundOrderForm, OutboundOrderEntity.class);
        List<OutboundEntryEntityInfoModel> entryList = outboundOrderForm.getEntryList() != null ? outboundOrderForm.getEntryList() : new ArrayList<>();
        List<OutboundEntryEntity> outboundEntryEntityList = JsonUtil.getJsonToList(entryList, OutboundEntryEntity.class);
        entity.setId(id);
        QueryWrapper<OutboundEntryEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OutboundEntryEntity::getOutboundId, entity.getId());
        outboundEntryService.remove(queryWrapper);
        for (int i = 0; i < outboundEntryEntityList.size(); i++) {
            outboundEntryEntityList.get(i).setId(RandomUtil.uuId());
            outboundEntryEntityList.get(i).setOutboundId(entity.getId());
            outboundEntryEntityList.get(i).setSortCode(Long.parseLong(i + ""));
            outboundEntryService.save(outboundEntryEntityList.get(i));
        }
        //编辑
        this.saveOrUpdate(entity);
    }
}
