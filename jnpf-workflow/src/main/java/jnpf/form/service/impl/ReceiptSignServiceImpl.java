package jnpf.form.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.base.service.BillRuleService;
import jnpf.base.util.FileManageUtil;
import jnpf.engine.service.FlowTaskService;
import jnpf.exception.WorkFlowException;
import jnpf.form.entity.ReceiptSignEntity;
import jnpf.form.mapper.ReceiptSignMapper;
import jnpf.form.model.receiptsign.ReceiptSignForm;
import jnpf.form.service.ReceiptSignService;
import jnpf.engine.util.ModelUtil;
import jnpf.model.FileModel;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 收文签呈单
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月29日 上午9:18
 */
@Service
public class ReceiptSignServiceImpl extends ServiceImpl<ReceiptSignMapper, ReceiptSignEntity> implements ReceiptSignService {

    @Autowired
    private BillRuleService billRuleService;
    @Autowired
    private FlowTaskService flowTaskService;
    @Autowired
    private FileManageUtil fileManageUtil;

    @Override
    public ReceiptSignEntity getInfo(String id) {
        QueryWrapper<ReceiptSignEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ReceiptSignEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = WorkFlowException.class)
    public void save(String id, ReceiptSignEntity entity) throws WorkFlowException {
        //表单信息
        if (id == null) {
            entity.setId(RandomUtil.uuId());
            //解析
            entity.setReceiptPaper(entity.getReceiptPaper());
            this.save(entity);
            billRuleService.useBillNumber("WF_ReceiptSignNo");
            //添加附件
            List<FileModel> data = JsonUtil.getJsonToList(entity.getFileJson(), FileModel.class);
            fileManageUtil.createFile(data);
        } else {
            entity.setId(id);
            //解析
            entity.setReceiptPaper(entity.getReceiptPaper());
            this.updateById(entity);
            //更新附件
            List<FileModel> data = JsonUtil.getJsonToList(entity.getFileJson(), FileModel.class);
            fileManageUtil.updateFile(data);
        }
        //流程信息
        ModelUtil.save(id, entity.getFlowId(), entity.getId(), entity.getFlowTitle(), entity.getFlowUrgent(), entity.getBillNo(),entity);
    }

    @Override
    @Transactional(rollbackFor = WorkFlowException.class)
    public void submit(String id, ReceiptSignEntity entity) throws WorkFlowException {
        //表单信息
        if (id == null) {
            entity.setId(RandomUtil.uuId());
            save(entity);
            billRuleService.useBillNumber("WF_ReceiptSignNo");
            //添加附件
            List<FileModel> data = JsonUtil.getJsonToList(entity.getFileJson(), FileModel.class);
            fileManageUtil.createFile(data);
        } else {
            entity.setId(id);
            this.updateById(entity);
            //更新附件
            List<FileModel> data = JsonUtil.getJsonToList(entity.getFileJson(), FileModel.class);
            fileManageUtil.updateFile(data);
        }
        //流程信息
        ModelUtil.submit(id, entity.getFlowId(), entity.getId(), entity.getFlowTitle(), entity.getFlowUrgent(), entity.getBillNo(), entity,null);
    }

    @Override
    public void data(String id, String data) {
        ReceiptSignForm receiptSignForm = JsonUtil.getJsonToBean(data, ReceiptSignForm.class);
        ReceiptSignEntity entity = JsonUtil.getJsonToBean(receiptSignForm, ReceiptSignEntity.class);
        entity.setId(id);
        this.saveOrUpdate(entity);
    }
}
