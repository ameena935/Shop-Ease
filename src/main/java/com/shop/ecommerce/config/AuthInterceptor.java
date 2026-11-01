package com.shop.ecommerce.config;

import com.shop.ecommerce.model.Role;
import com.shop.ecommerce.model.User;
import com.shop.ecommerce.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** Checks the "Authorization: Bearer <token>" header on protected /api routes. */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final UserRepository users;

    public AuthInterceptor(UserRepository users) { this.users = users; }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) return true;

        String header = req.getHeader("Authorization");
        User user = null;
        if (header != null && header.startsWith("Bearer ")) {
            user = users.findByToken(header.substring(7)).orElse(null);
        }
        if (user == null) return deny(res, 401, "Please login first");
        if (req.getRequestURI().startsWith("/api/admin") && user.getRole() != Role.ADMIN) {
            return deny(res, 403, "Admin access only");
        }
        req.setAttribute("user", user);
        return true;
    }

    private boolean deny(HttpServletResponse res, int code, String msg) throws Exception {
        res.setStatus(code);
        res.setContentType("application/json");
        res.getWriter().write("{\"message\":\"" + msg + "\"}");
        return false;
    }
}
