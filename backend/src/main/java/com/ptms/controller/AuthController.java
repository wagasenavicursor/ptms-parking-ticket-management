package com.ptms.controller;

import com.ptms.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {
    private static final int REMEMBER_ME_SECONDS = 30 * 24 * 60 * 60;
    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/auth/login")
    public Map<String, Object> login(@RequestBody Map<String, Object> request,
                                     HttpSession session,
                                     HttpServletRequest servletRequest,
                                     HttpServletResponse response) {
        Map<String, Object> user = service.login(text(request.get("username")), text(request.get("password")));
        session.setAttribute("username", user.get("username"));
        session.setAttribute("role", String.valueOf(user.get("role")));

        boolean rememberMe = Boolean.TRUE.equals(request.get("rememberMe"));
        session.setMaxInactiveInterval(rememberMe ? REMEMBER_ME_SECONDS : 30 * 60);
        addSessionCookie(response, servletRequest, session.getId(), rememberMe ? REMEMBER_ME_SECONDS : -1);
        return user;
    }

    @GetMapping("/auth/me")
    public ResponseEntity<?> currentUser(HttpSession session) {
        Object username = session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(service.current(String.valueOf(username)));
    }

    @PostMapping("/auth/logout")
    public void logout(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        session.invalidate();
        addSessionCookie(response, request, "", 0);
    }

    @PostMapping("/auth/forgot-password")
    public Map<String, String> forgot(@RequestBody Map<String, String> request) {
        return Map.of("message", service.forgot(request.get("email")));
    }

    @PostMapping("/auth/reset-password")
    public void reset(@RequestBody Map<String, String> request) {
        service.reset(request.get("token"), request.get("newPassword"));
    }

    @PostMapping("/auth/change-password")
    public void change(@RequestBody Map<String, String> request) {
        service.change(request.get("username"), request.get("currentPassword"), request.get("newPassword"));
    }

    @GetMapping("/users")
    public List<Map<String, Object>> users() {
        return service.users();
    }

    @PostMapping("/users")
    public Map<String, Object> create(@RequestBody Map<String, Object> request) {
        return service.save(null, request);
    }

    @PutMapping("/users/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        return service.save(id, request);
    }

    @DeleteMapping("/users/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    private void addSessionCookie(HttpServletResponse response, HttpServletRequest request, String value, int maxAge) {
        Cookie cookie = new Cookie("JSESSIONID", value);
        String contextPath = request.getContextPath();
        cookie.setPath(contextPath == null || contextPath.isBlank() ? "/" : contextPath);
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
