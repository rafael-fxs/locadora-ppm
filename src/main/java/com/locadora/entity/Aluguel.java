package com.locadora.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Aluguel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "jogo_id", nullable = false)
    private Jogo jogo;

    private LocalDate dataAluguel;
    private LocalDate dataPrevista;
    private LocalDate dataDevolucao;

    private double valorAluguel;
    private double valorMulta;


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Jogo getJogo() { return jogo; }
    public void setJogo(Jogo jogo) { this.jogo = jogo; }

    public LocalDate getDataAluguel() { return dataAluguel; }
    public void setDataAluguel(LocalDate dataAluguel) { this.dataAluguel = dataAluguel; }

    public LocalDate getDataPrevista() { return dataPrevista; }
    public void setDataPrevista(LocalDate dataPrevista) { this.dataPrevista = dataPrevista; }

    public LocalDate getDataDevolucao() { return dataDevolucao; }
    public void setDataDevolucao(LocalDate dataDevolucao) { this.dataDevolucao = dataDevolucao; }

    public double getValorAluguel() { return valorAluguel; }
    public void setValorAluguel(double valorAluguel) { this.valorAluguel = valorAluguel; }

    public double getValorMulta() { return valorMulta; }
    public void setValorMulta(double valorMulta) { this.valorMulta = valorMulta; }
}