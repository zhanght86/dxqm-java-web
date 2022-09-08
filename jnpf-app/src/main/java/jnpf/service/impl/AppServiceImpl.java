package jnpf.service.impl;

import jnpf.base.UserInfo;
import jnpf.model.AppUserInfoVO;
import jnpf.model.AppUsersVO;
import jnpf.model.login.UserPositionVO;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.entity.PositionEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.OrganizeService;
import jnpf.permission.service.PositionService;
import jnpf.permission.service.RoleService;
import jnpf.permission.service.UserService;
import jnpf.service.AppService;
import jnpf.util.JsonUtil;
import jnpf.util.UploaderUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * app用户信息
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021-07-08
 */
@Service
public class AppServiceImpl implements AppService {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private UserService userService;
    @Autowired
    private PositionService positionService;
    @Autowired
    private OrganizeService organizeService;
    @Autowired
    private RoleService roleService;

    @Override
    public AppUsersVO userInfo() {
        UserInfo userInfo = userProvider.get();
        AppUsersVO usersVO = new AppUsersVO();
        UserEntity userEntity = userService.getInfo(userInfo.getUserId());
        if (userEntity != null) {
            usersVO = JsonUtil.getJsonToBean(userEntity, AppUsersVO.class);
            this.userInfo(usersVO, userInfo);
            //角色
            List<String> roleId = Arrays.asList(userEntity.getRoleId().split(","));
            List<String> roleName = roleService.getRoleName(roleId).stream().map(t -> t.getFullName()).collect(Collectors.toList());
            usersVO.setRoleName(String.join(",", roleName));
            //部门
            OrganizeEntity depart = organizeService.getInfo(usersVO.getDepartmentId());
            usersVO.setDepartmentName(depart != null ? depart.getFullName() : "");
            //公司
            OrganizeEntity organize = organizeService.getInfo(usersVO.getOrganizeId());
            usersVO.setOrganizeName(organize != null ? organize.getFullName() : "");
            //岗位
            List<String> positionId = Arrays.asList(userEntity.getPositionId().split(","));
            List<PositionEntity> positionName = positionService.getPositionName(positionId);
            List<UserPositionVO> positionIds = new ArrayList<>();
            for (PositionEntity position : positionName) {
                UserPositionVO positionVO = new UserPositionVO();
                positionVO.setId(position.getId());
                positionVO.setName(position.getFullName());
                positionIds.add(positionVO);
            }
            usersVO.setPositionIds(positionIds);
        }
        return usersVO;
    }

    @Override
    public AppUserInfoVO getInfo(String id) {
        AppUserInfoVO userInfoVO = new AppUserInfoVO();
        UserEntity entity = userService.getInfo(id);
        if (entity != null) {
            userInfoVO = JsonUtil.getJsonToBean(entity, AppUserInfoVO.class);
            List<String> positionIds = Arrays.asList(entity.getPositionId().split(","));
            List<String> positionName = positionService.getPositionName(positionIds).stream().map(t -> t.getFullName()).collect(Collectors.toList());
            userInfoVO.setPositionName(String.join(",", positionName));
            OrganizeEntity info = organizeService.getInfo(entity.getOrganizeId());
            userInfoVO.setOrganizeName(info != null ? info.getFullName() : "");
            userInfoVO.setHeadIcon(UploaderUtil.uploaderImg(userInfoVO.getHeadIcon()));
        }
        return userInfoVO;
    }

    /**
     * 登录信息
     *
     * @param appUsersVO 返回对象
     * @param userInfo   回话信息
     * @return
     */
    private void userInfo(AppUsersVO appUsersVO, UserInfo userInfo) {
        appUsersVO.setUserId(userInfo.getUserId());
        appUsersVO.setHeadIcon(UploaderUtil.uploaderImg(userInfo.getUserIcon()));
        appUsersVO.setOrganizeId(userInfo.getOrganizeId());
        appUsersVO.setDepartmentId(userInfo.getDepartmentId());
        appUsersVO.setUserName(userInfo.getUserName());
        appUsersVO.setUserAccount(userInfo.getUserAccount());
    }

}
