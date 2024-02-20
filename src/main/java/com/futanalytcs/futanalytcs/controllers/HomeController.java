package com.futanalytcs.futanalytcs.controllers;

import java.io.UnsupportedEncodingException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.futanalytcs.futanalytcs.servico.CookieService;

import jakarta.servlet.http.HttpServletRequest;


@Controller
public class HomeController {
	@GetMapping("/index")
	public String adm(Model model, HttpServletRequest request) throws UnsupportedEncodingException {
		model.addAttribute("nome", com.futanalytcs.futanalytcs.servico.CookieService.getCookie(request, "nomeUsuario"));
		
		return "/index";
	}
	
	
}
