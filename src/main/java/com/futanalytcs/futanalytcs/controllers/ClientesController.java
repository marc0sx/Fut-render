package com.futanalytcs.futanalytcs.controllers;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.futanalytcs.futanalytcs.models.Cliente;
import com.futanalytcs.futanalytcs.repositorio.ClientesRepo;
import com.futanalytcs.futanalytcs.servico.CookieService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ClientesController {
	
	@Autowired
	private ClientesRepo repo;
	
	
	@GetMapping("/clientes")
	public String clientes(Model model, HttpServletRequest request) throws UnsupportedEncodingException {
		List<Cliente> clientes = (List<Cliente>)repo.findAll();
		model.addAttribute("clientes", clientes);
		model.addAttribute("nome", CookieService.getCookie(request, "nomeUsuario"));
		return "clientes/index";
	}
	
	@GetMapping("/clientes/new")
	public String clienteNew() {
		return "clientes/clienteNew";
	}
	
	@PostMapping("/clientes/criar")
	public String criar(Cliente cliente) {
		repo.save(cliente);
		return "redirect:/clientes";
	}
	
	@GetMapping("/clientes/{id}")
	public String busca(@PathVariable int id, Model model) {
		Optional<Cliente> client = repo.findById(id);
		try {
			model.addAttribute("cliente", client.get());
		} catch(Exception err){return "redirect:/clientes";}
				
		return "clientes/editar";
	}
	
	@PostMapping("/clientes/{id}/alterar")
	public String alterar(@PathVariable int id, Cliente cliente) {
		if (!repo.existsById(id)){
		return "redirect:/clientes";
		}
		repo.save(cliente);
				
		return "redirect:/clientes";
	}
	
	@GetMapping("/clientes/{id}/excluir")
	public String excluir(@PathVariable int id) {
		repo.deleteById(id);
		return "redirect:/clientes";
	}
}
