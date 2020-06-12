package org.jeecg.common.exception;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
@Slf4j
public class RequestFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        String path = ((HttpServletRequest) servletRequest).getRequestURI();
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        }catch (Exception e){
            String message = e.getMessage();
            Result<Object> response = null;
            if (message.indexOf("Token失效，请重新登录")>-1){
                response = Result.error(502, message);

            }else {
                response = Result.error(500, message);
            }
            servletResponse.setCharacterEncoding("utf-8");
            servletResponse.getWriter().print(JSON.toJSONString(response));
        }

    }

}