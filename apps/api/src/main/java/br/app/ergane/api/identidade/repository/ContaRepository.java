package br.app.ergane.api.identidade.repository;

import br.app.ergane.api.identidade.model.Conta;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ContaRepository extends JpaRepository<Conta, UUID> {

}
