# Locadora de Jogos - Sistema de Gerenciamento

Este projeto é um sistema de gerenciamento para uma locadora de jogos, construído em Java com o framework Spring Boot. A aplicação permite gerenciar o aluguel de jogos, cadastro de clientes, controle de assinaturas e aplicação de regras de negócio, como descontos e cálculo de multas.

## Funcionalidades Principais

O sistema fornece as seguintes funcionalidades:

1. **Cadastro e Gerenciamento de Clientes**: Permite registrar, atualizar, buscar e excluir informações de clientes da locadora.
2. **Cadastro de Jogos**: Jogos disponíveis para aluguel podem ser cadastrados, atualizados e removidos.
3. **Gerenciamento de Assinaturas**: Clientes podem ter diferentes tipos de assinaturas (Básico, Premium), que influenciam nas condições de aluguel.
4. **Aluguel de Jogos**: Controle dos jogos alugados, com registro de data de aluguel, data de devolução e valor cobrado.
5. **Descontos e Multas**: Aplicação de descontos para assinantes e cálculo de multas para devoluções atrasadas.

## Melhorias e Padrões de Projeto

Para tornar o sistema mais modular, flexível e fácil de manter, foram identificados cinco pontos de melhoria utilizando três padrões de projeto diferentes. Abaixo está uma visão geral dessas melhorias:

### 1. Criação de Assinaturas com o Padrão Factory
No código atual, o método `cadastrarAssinatura` em `AssinaturaService` usa uma estrutura condicional `switch` para instanciar diferentes tipos de assinaturas. Isso faz com que o código seja menos flexível e propenso a erros se novos tipos de assinatura forem adicionados.

**Antes (Código Atual)**
```java
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
```

**Depois (Usando o Padrão Factory)**

Aqui, usamos uma `AssinaturaFactory` para cada tipo de assinatura, separando a lógica de criação de cada tipo em suas próprias classes.
```java
public class AssinaturaBasicaFactory implements AssinaturaFactory {
    @Override
    public Assinatura criarAssinatura() {
        Assinatura assinatura = new Assinatura();
        assinatura.setTipo("Básico");
        assinatura.setDesconto(10.0);
        assinatura.setDiasExtras(3);
        assinatura.setEliminaMulta(false);
        return assinatura;
    }
}

public class AssinaturaPremiumFactory implements AssinaturaFactory {
    @Override
    public Assinatura criarAssinatura() {
        Assinatura assinatura = new Assinatura();
        assinatura.setTipo("Premium");
        assinatura.setDesconto(20.0);
        assinatura.setDiasExtras(7);
        assinatura.setEliminaMulta(true);
        return assinatura;
    }
}
```


