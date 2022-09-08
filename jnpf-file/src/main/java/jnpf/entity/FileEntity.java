package jnpf.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date  2021/5/13
 */
@Data
@TableName("base_file")
public class FileEntity {
    /**
     * 主键
     */
    @TableId("F_Id")
    private String fileId;

    /**
     * 文件编辑版本
     */
    @TableField("F_FileVersion")
    private String fileVersionId;

    /**
     * 文件名
     */
    @TableField("F_FileName")
    private String fileName;

    /**
     * 文件上传方式
     */
    @TableField("F_Type")
    private String type;

    /**
     * 上传的url
     */
    @TableField("F_Url")
    private String url;

    /**
     * 上次文件版本
     */
    @TableField("F_OldFileVersionId")
    private String oldFileVersionId;
}
