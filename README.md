# Stockly — Android Inventory Manager

Aplicação mobile de gestão de stock desenvolvida em Kotlin para Android.

O Stockly permite gerir produtos, quantidades e movimentos de stock através de uma interface moderna, intuitiva e responsiva.

## Estado do projeto

Concluído — projeto desenvolvido para avaliação prática e apresentação em portefólio.

## Funcionalidades

- Splash Screen personalizada;
- Sistema de autenticação local;
- Utilizadores persistidos localmente;
- Passwords armazenadas através de hash SHA-256;
- Dashboard com resumo do inventário;
- Registo de produtos;
- Consulta de produtos;
- Pesquisa de produtos por nome;
- Filtro por categoria;
- Filtro por estado do stock;
- Ordenação de produtos;
- Edição de produtos;
- Eliminação de produtos;
- Definição de stock mínimo;
- Identificação de produtos com stock reduzido;
- Registo de movimentos de entrada e saída;
- Validação das quantidades movimentadas;
- Histórico de movimentos de stock;
- Logout.

## Tecnologias

- Kotlin;
- Android SDK;
- XML Layouts;
- Material Design 3;
- Room Database;
- RecyclerView;
- ViewModel;
- Kotlin Coroutines;
- View Binding;
- Android Architecture Components.

## Arquitetura

A aplicação está organizada por camadas, seguindo uma abordagem baseada em separação de responsabilidades:

- `data` — entidades Room, DAOs e repositórios;
- `ui` — Activities, ViewModels e componentes relacionados com a interface;
- `utils` — funções e recursos auxiliares, quando aplicável.

## Persistência local

Os dados da aplicação são armazenados localmente através do Room Database.

A base de dados inclui:

- Produtos;
- Movimentos de stock;
- Utilizadores.

As credenciais dos utilizadores não são armazenadas diretamente em texto simples. As passwords são convertidas para SHA-256 antes de serem persistidas.

## Utilizadores iniciais

A aplicação disponibiliza três utilizadores iniciais para autenticação:

| Utilizador | Password |
|------------|----------|
| admin | password123 |
| cesae | cesae |
| angela | stockly2026 |

## Autora

**Ângela Pereira**

Software Developer · UX/UI Designer
