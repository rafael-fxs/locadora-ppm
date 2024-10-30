package com.locadora.controller;

import com.locadora.entity.Jogo;
import com.locadora.repository.JogoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jogos")
public class JogoController {

    @Autowired
    private JogoRepository jogoRepository;

    @GetMapping
    public ResponseEntity<List<Jogo>> listarJogos() {
        List<Jogo> jogos = jogoRepository.findAll();
        return ResponseEntity.ok(jogos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Jogo> buscarJogoPorId(@PathVariable Long id) {
        Jogo jogo = jogoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Jogo não encontrado"));
        return ResponseEntity.ok(jogo);
    }

    @PostMapping
    public ResponseEntity<Jogo> cadastrarJogo(@RequestBody Jogo jogo) {
        Jogo novoJogo = jogoRepository.save(jogo);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoJogo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Jogo> atualizarJogo(@PathVariable Long id, @RequestBody Jogo jogoAtualizado) {
        Jogo jogo = jogoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Jogo não encontrado"));

        jogo.setTitulo(jogoAtualizado.getTitulo());
        jogo.setPlataforma(jogoAtualizado.getPlataforma());
        jogo.setClassificacaoEtaria(jogoAtualizado.getClassificacaoEtaria());
        jogo.setEstoque(jogoAtualizado.getEstoque());
        jogo.setPreco(jogoAtualizado.getPreco());
        jogo.setDesconto(jogoAtualizado.getDesconto());
        Jogo jogoSalvo = jogoRepository.save(jogo);

        return ResponseEntity.ok(jogoSalvo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarJogo(@PathVariable Long id) {
        Jogo jogo = jogoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Jogo não encontrado"));

        jogoRepository.delete(jogo);
        return ResponseEntity.noContent().build();
    }
}
