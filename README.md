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

### 1. Criação de Assinaturas com o `Padrão Factory`
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

### 2. Aplicação de Descontos com o `Padrão Decorator`

Atualmente, o cálculo de desconto está diretamente no `AluguelService`. Se novos tipos de desconto forem adicionados, o código poderá se tornar difícil de manter.

**Antes (Código Atual)**
```java
public Aluguel alugarJogo(Long clienteId, Long jogoId) {
    Cliente cliente = clienteRepository.findById(clienteId).orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
    Jogo jogo = jogoRepository.findById(jogoId).orElseThrow(() -> new RuntimeException("Jogo não encontrado"));

    double valorAluguel = jogo.getPrecoComDesconto();
    if (cliente.getAssinatura() != null) {
        valorAluguel -= (valorAluguel * cliente.getAssinatura().getDesconto() / 100);
    }

    ...
}
```

**Depois (Usando o Padrão Factory)**

Com o `Decorator`, a aplicação de desconto é feita através da classe `AluguelComDesconto`, encapsulando o cálculo em uma estrutura que permite aplicar descontos de forma modular.

```java
public interface AluguelDesconto {
    double calcularValorComDesconto(double valorBase);
}

public class AluguelDescontoBase implements AluguelDesconto {
    @Override
    public double calcularValorComDesconto(double valorBase) {
        return valorBase;
    }
}

public class AluguelComDesconto implements AluguelDesconto {
    private final AluguelDesconto aluguelDesconto;
    private final double percentualDesconto;

    public AluguelComDesconto(AluguelDesconto aluguelDesconto, double percentualDesconto) {
        this.aluguelDesconto = aluguelDesconto;
        this.percentualDesconto = percentualDesconto;
    }

    @Override
    public double calcularValorComDesconto(double valorBase) {
        double valorComDesconto = aluguelDesconto.calcularValorComDesconto(valorBase);
        return valorComDesconto - (valorComDesconto * percentualDesconto / 100);
    }
}
```

No `AluguelService`:

```java
public Aluguel alugarJogo(Long clienteId, Long jogoId) {
    Cliente cliente = clienteRepository.findById(clienteId).orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
    Jogo jogo = jogoRepository.findById(jogoId).orElseThrow(() -> new RuntimeException("Jogo não encontrado"));

    AluguelDesconto aluguelDesconto = new AluguelDescontoBase();
    if (cliente.getAssinatura() != null) {
        aluguelDesconto = new AluguelComDesconto(aluguelDesconto, cliente.getAssinatura().getDesconto());
    }

    double valorAluguel = aluguelDesconto.calcularValorComDesconto(jogo.getPrecoComDesconto());
}
```

### 3. Cálculo de Multas com o `Padrão Strategy`

A lógica de cálculo de multa no `processarDevolucao` mistura diferentes condições para tipos de clientes. Isso complica o código se novos tipos de clientes com condições de multa diferentes forem adicionados.

**Antes (Código Atual)**

```java
public double processarDevolucao(Long clienteId, Long jogoId, LocalDate dataDevolucao) {
    Cliente cliente = clienteRepository.findById(clienteId).orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
    Jogo jogo = jogoRepository.findById(jogoId).orElseThrow(() -> new RuntimeException("Jogo não encontrado"));

    Aluguel aluguel = aluguelRepository.findByClienteAndJogoAndDataDevolucaoIsNull(cliente, jogo)
            .orElseThrow(() -> new RuntimeException("Aluguel não encontrado ou já devolvido"));

    long diasAtraso = ChronoUnit.DAYS.between(aluguel.getDataPrevista(), dataDevolucao);
    double valorMulta = 0;
    if (diasAtraso > 0 && (cliente.getAssinatura() == null || !cliente.getAssinatura().isEliminaMulta())) {
        valorMulta = diasAtraso * 5.0;
    }

    ...
}
```

**Depois (Usando o Padrão Strategy)**

Com o `Strategy Pattern`, encapsulamos o cálculo de multa em estratégias, facilitando a adição de novos tipos de cálculo.

```java
public interface MultaStrategy {
    double calcularMulta(long diasAtraso);
}

public class MultaPadraoStrategy implements MultaStrategy {
    @Override
    public double calcularMulta(long diasAtraso) {
        return diasAtraso * 5.0;
    }
}

public class MultaPremiumStrategy implements MultaStrategy {
    @Override
    public double calcularMulta(long diasAtraso) {
        return 0; // Sem multa para clientes Premium
    }
}
```

No `AluguelService`:

```java
public double processarDevolucao(Long clienteId, Long jogoId, LocalDate dataDevolucao) {
    Cliente cliente = clienteRepository.findById(clienteId).orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
    Jogo jogo = jogoRepository.findById(jogoId).orElseThrow(() -> new RuntimeException("Jogo não encontrado"));

    Aluguel aluguel = aluguelRepository.findByClienteAndJogoAndDataDevolucaoIsNull(cliente, jogo)
            .orElseThrow(() -> new RuntimeException("Aluguel não encontrado ou já devolvido"));

    MultaStrategy multaStrategy = cliente.getAssinatura() != null && cliente.getAssinatura().isEliminaMulta()
            ? new MultaPremiumStrategy() : new MultaPadraoStrategy();

    long diasAtraso = ChronoUnit.DAYS.between(aluguel.getDataPrevista(), dataDevolucao);
    double valorMulta = multaStrategy.calcularMulta(diasAtraso);

    ...
}
```

### 4. Uso de Interfaces para Repositórios e Serviços

Atualmente, os serviços e repositórios são implementados diretamente como classes concretas. Isso dificulta o teste isolado de funcionalidades e a troca de implementações, se necessário.
Usar interfaces para definir os métodos dos serviços e repositórios torna o código mais modular, flexível e fácil de testar. Com `interfaces`, é possível substituir implementações sem modificar o código principal, o que facilita a manutenção e expansão futura do sistema.

**Antes (Código Atual)**

```java
@Service
public class ClienteService {
    @Autowired
    private ClienteRepository clienteRepository;

    public Cliente buscarClientePorId(Long id) {
        return clienteRepository.findById(id).orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
    }
}
```

**Depois (Com Interface para Serviço)**

```java
public interface ClienteService {
    Cliente buscarClientePorId(Long id);
}

@Service
public class ClienteServiceImpl implements ClienteService {
    @Autowired
    private ClienteRepository clienteRepository;

    @Override
    public Cliente buscarClientePorId(Long id) {
        return clienteRepository.findById(id).orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
    }
}
```

### 5. Separação das Camadas de Responsabilidade

O método `alugarJogo` no `AluguelService` concentra múltiplas responsabilidades, como validação de idade, cálculo de desconto, atualização de estoque e criação do aluguel, tornando-o menos modular e difícil de manter. Para melhorar, cada tarefa foi separada: a validação de idade agora usa `ValidadorIdadeService`, o cálculo de desconto é feito com o padrão `Decorator`, o controle de estoque é gerido pelo `EstoqueService`, e a criação do aluguel foi movida para um método dedicado, `criarAluguel`.

**Antes (Método alugarJogo com múltiplas responsabilidades):**

```java
public Aluguel alugarJogo(Long clienteId, Long jogoId) {
    Cliente cliente = clienteRepository.findById(clienteId)
            .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
    Jogo jogo = jogoRepository.findById(jogoId)
            .orElseThrow(() -> new RuntimeException("Jogo não encontrado"));

    // Validação de idade
    if (cliente.getIdade() < jogo.getClassificacaoEtaria()) {
        throw new RuntimeException("Cliente não tem idade suficiente para alugar este jogo");
    }

    // Cálculo do valor do aluguel com desconto
    double valorAluguel = jogo.getPrecoComDesconto();
    if (cliente.getAssinatura() != null) {
        valorAluguel -= (valorAluguel * cliente.getAssinatura().getDesconto() / 100);
    }

    // Atualiza o estoque e salva o aluguel
    jogo.setEstoque(jogo.getEstoque() - 1);
    jogoRepository.save(jogo);

    Aluguel aluguel = new Aluguel();
    aluguel.setCliente(cliente);
    aluguel.setJogo(jogo);
    aluguel.setDataAluguel(LocalDate.now());
    aluguel.setValorAluguel(valorAluguel);
    aluguelRepository.save(aluguel);

    return aluguel;
}
```

**Depois (Separação de Responsabilidades)**

Serviço `ValidadorIdadeService`: Encapsula a validação da idade mínima.

```java
@Service
public class ValidadorIdadeService {
    public void validarIdadeParaJogo(Cliente cliente, Jogo jogo) {
        if (cliente.getIdade() < jogo.getClassificacaoEtaria()) {
            throw new RuntimeException("Cliente não tem idade suficiente para alugar este jogo");
        }
    }
}
```

Serviço `EstoqueService`: Encapsula a lógica de manipulação de estoque para facilitar a reutilização em outras partes do código.

```java
@Service
public class EstoqueService {
    @Autowired
    private JogoRepository jogoRepository;

    public void reduzirEstoque(Jogo jogo) {
        if (jogo.getEstoque() <= 0) {
            throw new RuntimeException("Estoque insuficiente para o jogo selecionado");
        }
        jogo.setEstoque(jogo.getEstoque() - 1);
        jogoRepository.save(jogo);
    }
}
```

Método `criarAluguel` no `AluguelService`: Cria e salva a entidade Aluguel, facilitando a reutilização do processo de criação e salvamento do aluguel.

```java
private Aluguel criarAluguel(Cliente cliente, Jogo jogo, double valorAluguel) {
    Aluguel aluguel = new Aluguel();
    aluguel.setCliente(cliente);
    aluguel.setJogo(jogo);
    aluguel.setDataAluguel(LocalDate.now());
    aluguel.setValorAluguel(valorAluguel);
    return aluguelRepository.save(aluguel);
}
```

Ajuste no `AluguelService`: Agora, `alugarJogo` delega as responsabilidades de validação e atualização de estoque para outros serviços, e o cálculo do valor do aluguel permanece utilizando o padrão `Decorator` conforme implementado.

```java
@Service
public class AluguelService {
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private JogoRepository jogoRepository;
    @Autowired
    private AluguelRepository aluguelRepository;
    @Autowired
    private ValidadorIdadeService validadorIdadeService;
    @Autowired
    private EstoqueService estoqueService;

    public Aluguel alugarJogo(Long clienteId, Long jogoId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        Jogo jogo = jogoRepository.findById(jogoId)
                .orElseThrow(() -> new RuntimeException("Jogo não encontrado"));

        // Validação da idade usando ValidadorIdadeService
        validadorIdadeService.validarIdadeParaJogo(cliente, jogo);

        // Cálculo do valor do aluguel com desconto usando Decorator
        AluguelDesconto aluguelDesconto = new AluguelDescontoBase();
        if (cliente.getAssinatura() != null) {
            aluguelDesconto = new AluguelComDesconto(aluguelDesconto, cliente.getAssinatura().getDesconto());
        }
        double valorAluguel = aluguelDesconto.calcularValorComDesconto(jogo.getPrecoComDesconto());

        // Reduz o estoque usando EstoqueService
        estoqueService.reduzirEstoque(jogo);

        // Criação e salvamento do aluguel usando criarAluguel
        return criarAluguel(cliente, jogo, valorAluguel);
    }
}
```

