package jnpf.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.entity.OrderEntryEntity;
import jnpf.mapper.OrderEntryMapper;
import jnpf.service.OrderEntryService;
import org.springframework.stereotype.Service;

/**
 * 订单明细
 *
 * @copyright 引迈信息技术有限公司
 * @author JNPF开发平台组
 * @version V3.1.0
 * @date 2019年9月26日 上午9:18
 */
@Service
public class OrderEntryServiceImpl extends ServiceImpl<OrderEntryMapper, OrderEntryEntity> implements OrderEntryService {

}
