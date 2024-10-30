package com.locadora.repository;

import com.locadora.entity.Aluguel;
import com.locadora.entity.Cliente;
import com.locadora.entity.Jogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AluguelRepository extends JpaRepository<Aluguel, Long> {
    Optional<Aluguel> findByClienteAndJogoAndDataDevolucaoIsNull(Cliente cliente, Jogo jogo);
}