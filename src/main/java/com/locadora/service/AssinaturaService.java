package com.locadora.service;

import com.locadora.entity.Assinatura;
import com.locadora.entity.Cliente;
import com.locadora.entity.TipoAssinatura;
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

    public Assinatura cadastrarAssinatura(Long clienteId, TipoAssinatura tipoAssinatura) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        Assinatura assinatura = new Assinatura();

        switch (tipoAssinatura) {
            case BASICO:
                assinatura.setTipo("Básico");
                assinatura.setDesconto(10.0);
                assinatura.setDiasExtras(3);
                assinatura.setEliminaMulta(false);
                break;

            case PREMIUM:
                assinatura.setTipo("Premium");
                assinatura.setDesconto(20.0);
                assinatura.setDiasExtras(7);
                assinatura.setEliminaMulta(true);
                break;

            default:
                throw new RuntimeException("Tipo de assinatura inválido");
        }

        assinaturaRepository.save(assinatura);
        cliente.setAssinatura(assinatura);
        clienteRepository.save(cliente);

        return assinatura;
    }
}


