package com.example.session;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * 演示素材：Servlet 会话管理 — 登录并写入 Session
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        if (username == null || password == null || username.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "用户名或密码不能为空");
            return;
        }
        // 演示：固定账号 student1 / 123456
        if ("student1".equals(username) && "123456".equals(password)) {
            HttpSession session = req.getSession(true);
            session.setAttribute("user", username);
            session.setMaxInactiveInterval(30 * 60);
            resp.sendRedirect("/dashboard");
            return;
        }
        resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "登录失败");
    }
}
