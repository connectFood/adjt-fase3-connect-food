# Tech Challenge -- Fase 3

## Sistema de Pedido Online para Restaurante

Boas-vindas ao Tech Challenge da fase 3! Este desafio foca em
arquitetura distribuída, segurança de aplicações e comunicação entre
serviços.

------------------------------------------------------------------------

# 1. Problema a resolver

Implementar um conjunto de serviços em **Java** usando:

- Spring Boot
- ou Quarkus

Esses serviços devem suportar o fluxo completo de **pedidos online de um
restaurante**, incluindo:

- criação de pedidos
- comunicação com serviço de pagamento externo
- controle de status do pedido
- tratamento de falhas com resiliência

------------------------------------------------------------------------

# 2. Objetivo

Criar um sistema de pedidos online que permita:

- criar pedidos
- processar pagamentos
- controlar status dos pedidos

O sistema deve ser:

- resiliente
- seguro
- capaz de lidar com falhas e timeouts

------------------------------------------------------------------------

# 3. Requisitos para entrega

## Aplicação

- Aplicação funcionando com todos os serviços implementados.

## Testes

- Arquivo de testes de endpoints (Postman, Bruno, Insomnia).

## Docker

- Arquivo `compose.yml` para subir todos os serviços.

## Documentação

Deve conter:

- Desenho da arquitetura (C4, componentes ou sequência)
- Descrição do fluxo principal
- Identificação dos pontos de resiliência

## Código

- Repositório com todos os serviços.

## Vídeo

Vídeo de até **10 minutos** apresentando:

- funcionalidades
- arquitetura escolhida
- justificativa da arquitetura

------------------------------------------------------------------------

# 4. Requisitos Funcionais

## 4.1 Gerenciamento de usuários

- Criar cliente
- Autenticar cliente

------------------------------------------------------------------------

## 4.2 Criar Pedido

O pedido deve conter:

### Cliente

- ID do cliente extraído do **JWT**

### Restaurante

### Itens

Cada item contém:

- id do produto
- nome
- quantidade
- preço

O sistema deve:

1. Calcular valor total
2. Retornar ID do pedido
3. Solicitar confirmação

Não é necessário CRUD de itens.

------------------------------------------------------------------------

## 4.3 Consultas

- Consultar pedido por ID
- Consultar pedidos do cliente autenticado

------------------------------------------------------------------------

## 4.4 Processamento de Pagamento

Existe um serviço externo chamado:

`processamento-pagamento-externo`

Fluxo:

1. pedido criado
2. pagamento-service chamado
3. pagamento-service chama serviço externo

------------------------------------------------------------------------

## 4.5 Pagamento Pendente

Se o serviço estiver indisponível:

- pedido **não falha**
- status = `PENDENTE_PAGAMENTO`
- pedido enviado para fila

------------------------------------------------------------------------

## 4.6 Reprocessamento Automático

Quando o serviço voltar:

- pedidos pendentes são reprocessados
- status atualizado para `PAGO`

------------------------------------------------------------------------

## 4.7 Atualização Automática de Status

Após pagamento confirmado:

- pedido atualizado automaticamente

Opcional:

- integração com produção
- notificação

------------------------------------------------------------------------

# 5. Requisitos Não Funcionais

## 5.1 Arquitetura em Múltiplos Serviços

Arquitetura mínima:

- auth-service
- pedido-service
- pagamento-service

Opcional:

- restaurante-service
- api-gateway

------------------------------------------------------------------------

## 5.2 Segurança

- Spring Security
- JWT
- login gerando token
- perfis: cliente e admin
- endpoints protegidos

------------------------------------------------------------------------

## 5.3 Kafka

Eventos obrigatórios:

- pedido.criado
- pagamento.aprovado
- pagamento.pendente

Fluxo:

1. pedido-service publica pedido.criado
2. pagamento-service consome
3. processa pagamento
4. publica eventos

------------------------------------------------------------------------

## 5.4 Resiliência (Resilience4j)

Utilizar:

- Circuit Breaker
- Retry
- Timeout

Fallback:

- marcar pedido como `PENDENTE_PAGAMENTO`
- publicar evento `pagamento.pendente`

------------------------------------------------------------------------

## 5.5 Arquitetura

Seguir Clean ou Hexagonal Architecture:

- controller
- service / use cases
- domain
- infra
