package com.ptms.config;

import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RoleAuthorizationInterceptor implements HandlerInterceptor {
  public boolean preHandle(HttpServletRequest r, HttpServletResponse p, Object h) throws Exception {
    String path = r.getRequestURI();
    String method = r.getMethod();

    if (path.contains("/api/auth/login") || path.contains("/api/auth/forgot-password") || path.contains("/api/auth/reset-password")) return true;

    HttpSession session = r.getSession(false);
    String role = session == null ? null : (String) session.getAttribute("role");
    if (role == null) {
      p.sendError(401, "Login required");
      return false;
    }

    if (path.contains("/api/audit") && !"SUPER_USER".equals(role)) {
      p.sendError(403, "Super User access required");
      return false;
    }

    if ("SUPER_USER".equals(role)) return true;

    if (path.contains("/api/users")) {
      p.sendError(403, "Super User access required");
      return false;
    }

    if ("ADMIN".equals(role)) return true;

    if ("SECURITY".equals(role)) {
      boolean readEmployees = path.contains("/api/employees") && "GET".equals(method);
      boolean readDepartments = path.contains("/api/departments") && "GET".equals(method);
      boolean readTeams = path.contains("/api/teams") && "GET".equals(method);
      boolean allowed = path.contains("/api/tickets")
        || path.contains("/api/issues")
        || path.contains("/api/visitors")
        || path.contains("/api/dashboard")
        || path.contains("/api/auth/")
        || readEmployees
        || readDepartments
        || readTeams;

      if (!allowed) {
        p.sendError(403, "This role cannot access this function");
        return false;
      }
    }

    return true;
  }
}
