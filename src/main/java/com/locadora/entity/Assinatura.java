package com.locadora.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


@Entity
public class Assinatura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String tipo;
    private double desconto;
    private int diasExtras;
    private boolean eliminaMulta;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public double getDesconto() {
        return desconto;
    }

    public void setDesconto(double desconto) {
        this.desconto = desconto;
    }

    public int getDiasExtras() {
        return diasExtras;
    }

    public void setDiasExtras(int diasExtras) {
        this.diasExtras = diasExtras;
    }

    public boolean isEliminaMulta() {
        return eliminaMulta;
    }

    public void setEliminaMulta(boolean eliminaMulta) {
        this.eliminaMulta = eliminaMulta;
    }
}
        