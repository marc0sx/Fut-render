package com.futanalytcs.futanalytcs.servico.autenticacao;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.futanalytcs.futanalytcs.servico.CookieService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginInterceptor implements HandlerInterceptor {
	@Override
	public boolean preHandle
		(HttpServletRequest request, HttpServletResponse response, Object handler)
		throws Exception {
		
		System.out.println("Pre Handle method is Calling ");
		
		if(CookieService.getCookie(request, "usuariosId") != null) {
			return true;
		}
		
		response.sendRedirect("/login");
		return false;
	}

}
