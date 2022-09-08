package jnpf.form.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.base.service.BillRuleService;
import jnpf.engine.service.FlowTaskService;
import jnpf.exception.WorkFlowException;
import jnpf.form.entity.SupplementCardEntity;
import jnpf.form.mapper.SupplementCardMapper;
import jnpf.form.model.supplementcard.SupplementCardForm;
import jnpf.form.service.SupplementCardService;
import jnpf.engine.util.ModelUtil;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 补卡申请
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月29日 上午9:18
 */
@Service
public class SupplementCardServiceImpl extends ServiceImpl<SupplementCardMapper, SupplementCardEntity> implements SupplementCardService {

    @Autowired
    private BillRuleService billRuleService;
    @Autowired
    private FlowTaskService flowTaskService;

    @Override
    public SupplementCardEntity getInfo(String id) {
        QueryWrapper<SupplementCardEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SupplementCardEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = WorkFlowException.class)
    public void save(String id, SupplementCardEntity entity) throws WorkFlowException {
        //表单信息
        if (id == null) {
            entity.setId(RandomUtil.uuId());
            this.save(entity);
            billRuleService.useBillNumber("WF_SupplementCardNo");
        } else {
            entity.setId(id);
            this.updateById(entity);
        }
        //流程信息
        ModelUtil.save(id, entity.getFlowId(), entity.getId(), entity.getFlowTitle(), entity.getFlowUrgent(), entity.getBillNo(),entity);
    }

    @Override
    @Transactional(rollbackFor = WorkFlowException.class)
    public void submit(String id, SupplementCardEntity entity) throws WorkFlowException {
        //表单信息
        if (id == null) {
            entity.setId(RandomUtil.uuId());
            this.save(entity);
            billRuleService.useBillNumber("WF_SupplementCardNo");
        } else {
            entity.setId(id);
            this.updateById(entity);
        }
        //流程信息
        ModelUtil.submit(id, entity.getFlowId(), entity.getId(), entity.getFlowTitle(), entity.getFlowUrgent(), entity.getBillNo(), entity,null);
    }

    @Override
    public void data(String id, String data) {
        SupplementCardForm supplementCardForm = JsonUtil.getJsonToBean(data, SupplementCardForm.class);
        SupplementCardEntity entity = JsonUtil.getJsonToBean(supplementCardForm, SupplementCardEntity.class);
        entity.setId(id);
        this.saveOrUpdate(entity);
    }
}
