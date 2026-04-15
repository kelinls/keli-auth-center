package com.keli.clientserver.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

@Component
public class SecurityAuthorityUtils {

    private static RoleHierarchy roleHierarchy;

    // 注入 RoleHierarchy（Spring 会自动注入）
    @Autowired
    public void setRoleHierarchy(RoleHierarchy roleHierarchy) {
        SecurityAuthorityUtils.roleHierarchy = roleHierarchy;
    }

    /**
     * 获取当前用户实际拥有的所有权限（考虑角色继承）
     */
    private static Collection<? extends GrantedAuthority> getReachableAuthorities() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Collections.emptyList();
        }
        // RoleHierarchy 会计算所有可到达的权限（包括继承的）
        return roleHierarchy.getReachableGrantedAuthorities(auth.getAuthorities());
    }

    /**
     * 检查是否拥有某个角色（自动添加 ROLE_ 前缀）
     */
    public static boolean hasRole(String role) {
        String roleWithPrefix = "ROLE_" + role;
        return getReachableAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(roleWithPrefix));
    }

    /**
     * 检查是否拥有某个权限（精确匹配，不添加前缀）
     */
    public static boolean hasAuthority(String authority) {
        return getReachableAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }

    /**
     * 检查并抛出异常
     */
    public static void checkRole(String role) {
        if (!hasRole(role)) {
            throw new AccessDeniedException("Need role: " + role);
        }
    }

    public static void checkAuthority(String authority) {
        if (!hasAuthority(authority)) {
            throw new AccessDeniedException("Need authority: " + authority);
        }
    }
}