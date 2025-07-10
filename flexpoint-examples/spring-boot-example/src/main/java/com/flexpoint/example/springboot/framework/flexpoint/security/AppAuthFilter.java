package com.flexpoint.example.springboot.framework.flexpoint.security;

import cn.hutool.json.JSONUtil;
import com.flexpoint.example.springboot.framework.common.CommonResult;
import com.flexpoint.example.springboot.framework.constants.ApiConstants;
import com.flexpoint.example.springboot.framework.constants.ErrorCodeConstants;
import com.flexpoint.example.springboot.framework.flexpoint.context.SysAppContext;
import com.flexpoint.example.springboot.manager.SysAppManager;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * 应用接入认证过滤器
 * @author luoxianggan
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppAuthFilter implements Filter {

    private static final List<String> EXCLUDE_PATHS = Lists.newArrayList(
            "/api/v1/health"
    );

    private final SysAppManager sysAppManager;

    @SneakyThrows
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        log.debug("AppAuthFilter 开始处理请求: method={}, uri={}", 
                httpRequest.getMethod(), httpRequest.getRequestURI());

        // ✅ 白名单路径直接放行
        if (isExcludePath(httpRequest.getRequestURI())) {
            log.debug("请求路径在白名单中，直接放行: {}", httpRequest.getRequestURI());
            chain.doFilter(request, response);
            return;
        }

        String appCode = httpRequest.getHeader(ApiConstants.HEADER_APP_CODE);
        String token = httpRequest.getHeader(ApiConstants.HEADER_TOKEN);

        log.debug("AppAuthFilter 获取认证信息: appCode={}, token={}", appCode, token != null ? "***" : null);

        if (appCode == null || token == null) {
            log.warn("AppAuthFilter 认证失败: 缺少必要的认证信息 appCode={}, token={}", appCode, token != null ? "***" : null);
            this.responseError(httpResponse, CommonResult.error(ErrorCodeConstants.SYS_APP_APP_CODE_ERROR));
            return;
        }

        SysAppManager.SysAppDO sysApp = sysAppManager.getByCode(appCode);

        if (Objects.isNull(sysApp) || !sysApp.getIsActive()) {
            this.responseError(httpResponse, CommonResult.error(ErrorCodeConstants.SYS_APP_NOT_ENABLED));
            return;
        }

        if (!token.equals(sysApp.getToken())) {
            this.responseError(httpResponse, CommonResult.error(ErrorCodeConstants.SYS_APP_TOKEN_ERROR));
            return;
        }

        // 设置当前应用上下文
        SysAppContext.setAppCode(appCode);
        log.debug("AppAuthFilter 认证成功，设置应用上下文: appCode={}", appCode);

        // 认证通过，继续后续处理
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // 清除上下文
        SysAppContext.clear();
    }

    private boolean isExcludePath(String path) {
        return EXCLUDE_PATHS.stream().anyMatch(path::startsWith);
    }

    private void responseError(HttpServletResponse response, CommonResult<?> result) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(JSONUtil.toJsonStr(result));
    }
} 