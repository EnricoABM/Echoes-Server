# Introdução

Servidor de autenticação responsável pela gestão de credenciais e acesso de usuário para uma aplicação mobile e sistema IoT. 

O projeto visa implementar mecanismos de autenticação de usuários, gestão de credenciais e emissão de tokens de acesso, seguindo boas normas e conformidade com a Lei Geral de Proteção de Dados (LGPD). 

## Tecnologias Utilizadas
* Java
* Spring Boot
* Spring Security
* JSON Web Token
* MySQL
* Mailgun

## Como Executar

### 1. Pré-Requisitos
Dependências do Projeto
* Java 21 ou superior
* Maven
* MySQL
* Variáveis de ambiente configuradas

### 2. Configuração do Banco de Dados
Para execução do sistema, deve-se configurar um usuário e um banco de dados e associa-los ao sistema por meio das variáveis `DATABASE_URL`, `DATABASE_USER` e `DATABASE_PASSWORD`.
* Windows
```bash
set DATABASE_URL=jdbc:mysql://address:3306/database
set DATABASE_USER=user
set DATABASE_PASSWORD=password
```
* Linux / Mac
```bash
export DATABASE_URL=jdbc:mysql://address:3306/database
export DATABASE_USER=user
export DATABASE_PASSWORD=password
```

### 3. Configuração das demais variáveis
Além do banco de dados, é necessário configurar `JWT_SECRET` para emissão de tokens, e `API_KEY` para o serviço de notificação via serviço de e-mail. 
* Windows
```bash
set JWT_SECRET=my-secret
set API_KEY=api-key
```
* Linux / Mac
```bash
export JWT_SECRET=my-secret
export API_KEY=api-key
```

## Executando a Aplicação

* Faça clone do repositório em sua máquina e navegue até a pasta:
```bash
git clone https://github.com/EnricoABM/Echoes-Server.git
cd Echoes-Server
```

* Execute o plugin Maven:
```bash
./mvnw spring-boot:run
```

A aplicação executa por padrão em:
```bash
http://localhost:8080
```

# Endpoints

## Autenticação

### Login
```request
POST api/auth/login
```
```json
{
    "email": "your@example.com",
    "password": "password"
}
```

### MFA
```request
POST api/auth/login/2fa
```
```json
{
    "email": "your@example.com",
    "code": "code"
}
```

### Logout
```request
GET api/auth/logout
Headers:
    Authorization: Bearer $token
```

### Validar Token
```request
GET api/auth/validate-token
Headers: 
    Authorization: Bearer $token
```

## Cadastro

### Register
```request
POST api/auth/register
```
```json
{
    "name": "your",
    "email": "your@example.com",
    "password": "password"
}
```

### Validar E-mail
```request
POST api/auth/register/2fa
```
```json
{
    "email": "your@example.com",
    "code": "code"
}
```

## Gestão de Usuários

### Informação do Usuário
```request
GET api/users/me
Headers:
    Authorization: Bearer $token
```
