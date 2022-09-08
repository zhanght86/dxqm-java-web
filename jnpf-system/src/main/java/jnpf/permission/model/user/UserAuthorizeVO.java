package jnpf.permission.model.user;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
@Builder
public class UserAuthorizeVO {
    private List<UserAuthorizeModel> button;
    private List<UserAuthorizeModel> column;
    private List<UserAuthorizeModel> module;
    private List<UserAuthorizeModel> resource;
}
