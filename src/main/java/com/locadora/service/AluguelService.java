package com.locadora.service;

import com.locadora.entity.Aluguel;
import com.locadora.entity.Assinatura;
import com.locadora.entity.Cliente;
import com.locadora.entity.Jogo;
import com.locadora.repository.AluguelRepository;
import com.locadora.repository.ClienteRepository;
import com.locadora.repository.JogoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class AluguelService {

    private static final double MULTA_POR_DIA = 5.0;

    @Autowired
    private JogoRepository jogoRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private AluguelRepository aluguelRepository;

    public Aluguel alugarJogo(Long clienteId, Long jogoId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        Jogo jogo = jogoRepository.findById(jogoId)
                .orElseThrow(() -> new RuntimeException("Jogo não encontrado"));

        if (cliente.getIdade() < jogo.getClassificacaoEtaria()) {
            throw new RuntimeException("Cliente não tem idade suficiente para alugar este jogo");
        }

        if (jogo.getEstoque() <= 0) {
            throw new RuntimeException("Jogo fora de estoque");
        }

        double valorAluguel = jogo.getPrecoComDesconto();
        if (cliente.getAssinatura() != null) {
            Assinatura assinatura = cliente.getAssinatura();
            valorAluguel -= (valorAluguel * assinatura.getDesconto() / 100);
        }

        LocalDate dataAluguel = LocalDate.now();
        LocalDate dataPrevista = dataAluguel.plusDays(7);
        if (cliente.getAssinatura() != null) {
            dataPrevista = dataPrevista.plusDays(cliente.getAssinatura().getDiasExtras());
        }

        jogo.setEstoque(jogo.getEstoque() - 1);
        jogoRepository.save(jogo);

        Aluguel aluguel = new Aluguel();
        aluguel.setCliente(cliente);
        aluguel.setJogo(jogo);
        aluguel.setDataAluguel(dataAluguel);
        aluguel.setDataPrevista(dataPrevista);
        aluguel.setValorAluguel(valorAluguel);
        aluguelRepository.save(aluguel);

        return aluguel;
    }

    public double processarDevolucao(Long clienteId, Long jogoId, LocalDate dataDevolucao) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        Jogo jogo = jogoRepository.findById(jogoId)
                .orElseThrow(() -> new RuntimeException("Jogo não encontrado"));

        Aluguel aluguel = aluguelRepository.findByClienteAndJogoAndDataDevolucaoIsNull(cliente, jogo)
                .orElseThrow(() -> new RuntimeException("Aluguel não encontrado ou já devolvido"));

        long diasAtraso = ChronoUnit.DAYS.between(aluguel.getDataPrevista(), dataDevolucao);
        double valorMulta = 0;
        if (diasAtraso > 0 && (cliente.getAssinatura() == null || !cliente.getAssinatura().isEliminaMulta())) {
            valorMulta = diasAtraso * MULTA_POR_DIA;
        }

        jogo.setEstoque(jogo.getEstoque() + 1);
        jogoRepository.save(jogo);

        aluguel.setDataDevolucao(dataDevolucao);
        aluguel.setValorMulta(valorMulta);
        aluguelRepository.save(aluguel);

        return valorMulta;
    }
}