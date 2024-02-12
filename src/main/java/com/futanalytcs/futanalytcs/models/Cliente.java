package com.futanalytcs.futanalytcs.models;





import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "clientes")
public class Cliente {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;
	
	@Column(name = "name", length = 100, nullable = false)
	private String nome;
	
	@Column(name = "email", length = 180, nullable = false)
	private String email;
	
	@Column(name = "senha", length = 255, nullable = false)
	private String senha;
	
	
	@Column(name = "data_cadastro")
    @CreationTimestamp
    private LocalDateTime dataCadastro;
	
	@Column(name = "data_fim") // Nome da nova coluna de data
    @Temporal(TemporalType.TIMESTAMP) // Tipo de mapeamento para data e hora
    private LocalDateTime dataFim;
	
	@Column(name = "observacao", columnDefinition = "TEXT")
	private String observacao;


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}
	

	public LocalDateTime getData_cadastro() {
		return dataCadastro;
	}

	public void setData_cadastro(LocalDateTime data_cadastro) {
		this.dataCadastro = data_cadastro;
	}

	public LocalDateTime getData_fim() {
		return dataFim;
	}

	public void setData_fim(LocalDateTime data_fim) {
		this.dataFim = data_fim;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	
}
