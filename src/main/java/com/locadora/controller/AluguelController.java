package com.locadora.controller;

import com.locadora.entity.Aluguel;
import com.locadora.service.AluguelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/alugueis")
public class AluguelController {

    @Autowired
    private AluguelService aluguelService;

    @PostMapping("/alugar")
    public ResponseEntity<Aluguel> alugarJogo(@RequestParam Long clienteId, @RequestParam Long jogoId) {
        Aluguel aluguel = aluguelService.alugarJogo(clienteId, jogoId);
        return ResponseEntity.ok(aluguel);
    }

    @PostMapping("/devolver")
    public ResponseEntity<Double> devolverJogo(
            @RequestParam Long clienteId,
            @RequestParam Long jogoId,
            @RequestParam String dataDevolucao) {

        LocalDate dataDevolucaoParsed = LocalDate.parse(dataDevolucao);
        double multa = aluguelService.processarDevolucao(clienteId, jogoId, dataDevolucaoParsed);

        return ResponseEntity.ok(multa);
    }
}