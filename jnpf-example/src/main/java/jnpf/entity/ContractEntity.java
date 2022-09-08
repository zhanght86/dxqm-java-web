package jnpf.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 9:47
 */
@Data
@TableName("test_contract")
public class ContractEntity  {

    @TableId("F_ID")
    private String id;

    @TableField("F_CONTRACTNAME")
    private String contractName;


    @TableField("F_MYTELEPHONE")
    private String mytelePhone;

    @TableField("F_FILEJSON")
    private String fileJson;

}
