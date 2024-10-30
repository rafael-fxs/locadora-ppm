package com.locadora.controller;

import com.locadora.entity.Assinatura;
import com.locadora.entity.TipoAssinatura;
import com.locadora.service.AssinaturaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assinaturas")
public class AssinaturaController {

    @Autowired
    private AssinaturaService assinaturaService;

    @PostMapping("/cadastrar")
    public ResponseEntity<Assinatura> cadastrarAssinatura(
            @RequestParam Long clienteId,
            @RequestParam TipoAssinatura tipoAssinatura) {

        Assinatura assinatura = assinaturaService.cadastrarAssinatura(clienteId, tipoAssinatura);
        return ResponseEntity.ok(assinatura);
    }
}
