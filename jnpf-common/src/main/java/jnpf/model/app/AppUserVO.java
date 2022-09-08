package jnpf.model.app;

import lombok.Data;

import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:47
 */
@Data
public class AppUserVO {
    private AppInfoModel userInfo;
    private List<AppMenuModel> menuList;
    private List<AppFlowFormModel> flowFormList;
    private List<AppDataModel> appDataList;
}
