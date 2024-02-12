package com.futanalytcs.futanalytcs.repositorio;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.futanalytcs.futanalytcs.models.Cliente;

public interface ClientesRepo extends CrudRepository<Cliente, Integer>{
	
	@Query(value="select * from clientes where email = :email and senha =:senha", nativeQuery = true)
	public Cliente login(String email, String senha);
}
