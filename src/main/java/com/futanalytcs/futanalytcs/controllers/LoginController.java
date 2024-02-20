package com.futanalytcs.futanalytcs.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.futanalytcs.futanalytcs.models.Cliente;
import com.futanalytcs.futanalytcs.repositorio.ClientesRepo;
import com.futanalytcs.futanalytcs.servico.CookieService;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class LoginController {
	@Autowired
	private ClientesRepo repo;
	
	@GetMapping("/login")
	public String index() {
	 return "login/index";
	}
	
	@PostMapping("/logar")
	public String logar(Model model, Cliente clientParam, String lembrar, HttpServletResponse response) throws IOException {
	 Cliente client = this.repo.login(clientParam.getEmail(), clientParam.getSenha());
	 if (client != null) {
		 int tempoLogado = (60*60); // 1 hora de cookie
		 if(lembrar != null) tempoLogado = (60*60*24*365);
		 CookieService.setCookie(response, "usuariosId", String.valueOf(client.getId()), tempoLogado);
		 CookieService.setCookie(response, "nomeUsuario", String.valueOf(client.getNome()), tempoLogado);
		 return "redirect:/index";
	 }
	 model.addAttribute("erro", "Usuário ou senha inválidos");
	 return "login/index";
	}
	
	
	@GetMapping("/sair")
	public String sair(HttpServletResponse response) throws IOException {
		 CookieService.setCookie(response, "usuariosId", "", 0);
		 CookieService.setCookie(response, "nomeUsuario", "", 0);
		 return "redirect:/login";
	}
}
