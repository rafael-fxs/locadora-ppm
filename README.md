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

Algumas `Controllers` estão interagindo diretamente com repositórios. Isso resulta em uma mistura de responsabilidades, onde o controlador, além de lidar com requisições HTTP, também gerencia o acesso a dados e a lógica de negócios. Essa abordagem dificulta a manutenção e torna o código menos modular.

**Antes (Controlador acessando diretamente o repositório):**

```java
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    
    @Autowired
    private ClienteRepository clienteRepository;

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscarClientePorId(@PathVariable Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        return ResponseEntity.ok(cliente);
    }

    @GetMapping
    public ResponseEntity<List<Cliente>> listarClientes() {
        List<Cliente> clientes = clienteRepository.findAll();
        return ResponseEntity.ok(clientes);
    }
}
```

**Depois (Separação com Serviço):**

Interface ClienteService: Define as operações de negócio relacionadas a Cliente.

```java
public interface ClienteService {
    Cliente buscarClientePorId(Long id);
    List<Cliente> listarClientes();
}
```

Implementação ClienteServiceImpl: Concentra a lógica de negócios e interage com o repositório.

```java
@Service
public class ClienteServiceImpl implements ClienteService {
    @Autowired
    private ClienteRepository clienteRepository;

    @Override
    public Cliente buscarClientePorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
    }

    @Override
    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }
}
```

Controlador ClienteController: Agora usa o ClienteService para acessar a lógica de negócios, sem interagir diretamente com o repositório.

```java
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    
    @Autowired
    private ClienteService clienteService;

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscarClientePorId(@PathVariable Long id) {
        Cliente cliente = clienteService.buscarClientePorId(id);
        return ResponseEntity.ok(cliente);
    }

    @GetMapping
    public ResponseEntity<List<Cliente>> listarClientes() {
        List<Cliente> clientes = clienteService.listarClientes();
        return ResponseEntity.ok(clientes);
    }
}
```

