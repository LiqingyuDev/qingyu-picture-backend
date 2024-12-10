package com.qingyu.qingyupicturebackend.annotation;

import com.qingyu.qingyupicturebackend.exception.BusinessException;
import com.qingyu.qingyupicturebackend.exception.ErrorCode;
import com.qingyu.qingyupicturebackend.model.entity.User;
import com.qingyu.qingyupicturebackend.model.enums.UserRoleEnum;
import com.qingyu.qingyupicturebackend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 权限校验注解切面
 *
 * @author liqingyu.dev@gmail.com
 * @since 2024/12/10
 */
@Aspect
@Component
public class AuthInterceptor {
    @Resource
    private UserService userService;

    /**
     * 执行拦截逻辑
     *
     * @param point 切点
     * @param authCheck 权限校验注解
     * @return 处理结果
     * @throws Throwable 异常
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint point, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();

        // 获取当前请求的 HttpServletRequest 对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        // 获取当前登录用户的权限
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum concurrentRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());

        // 获取当前接口需要的权限
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);

        // 如果接口不需要特定权限，则直接放行
        if (mustRoleEnum == null) {
            return point.proceed();
        }

        // 以下为：必须有某种权限才通过
        // 1. 用户没有任何权限，拦截
        if (concurrentRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 2. 需要管理员权限，但是当前登录用户没有管理员权限，拦截
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(concurrentRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 3. 所有条件检查完成后，允许继续执行
        return point.proceed();
    }
}
