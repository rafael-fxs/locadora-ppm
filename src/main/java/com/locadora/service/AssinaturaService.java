package com.locadora.service;

import com.locadora.entity.Assinatura;
import com.locadora.entity.Cliente;
import com.locadora.repository.AssinaturaRepository;
import com.locadora.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AssinaturaService {

    @Autowired
    private AssinaturaRepository assinaturaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    public Assinatura cadastrarAssinatura(Long clienteId, String tipo) {
        Cliente cliente = clienteRepository.findById(clienteId).orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        Assinatura assinatura = new Assinatura();
        if ("Básico".equals(tipo)) {
            assinatura.setTipo("Básico");
            assinatura.setDesconto(10.0);
            assinatura.setDiasExtras(3);
            assinatura.setEliminaMulta(false);
        } else if ("Premium".equals(tipo)) {
            assinatura.setTipo("Premium");
            assinatura.setDesconto(20.0);
            assinatura.setDiasExtras(7);
            assinatura.setEliminaMulta(true);
        }

        assinaturaRepository.save(assinatura);
        cliente.setAssinatura(assinatura);
        clienteRepository.save(cliente);

        return assinatura;
    }
}

