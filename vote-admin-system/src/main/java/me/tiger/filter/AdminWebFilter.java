package me.tiger.filter;

import javax.servlet.*;
import java.io.IOException;

public class AdminWebFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // 开始转发
        servletRequest.getRequestDispatcher("/index.html").forward(servletRequest, servletResponse);
    }
}
