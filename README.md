# Introdução

Servidor de autenticação responsável pela gestão de credenciais e acesso de usuário para uma aplicação mobile e sistema IoT. 

O projeto implementa mecanismos de autenticação, autorização e emissão de tokens de acesso, seguindo boas práticas de segurança e conformidade com a Lei Geral de Proteção de Dados (LGPD).

## Tecnologias Utilizadas
* Java 21
* Spring Boot
* Spring Security
* JSON Web Token (JWT)
* MySQL
* Redis (rate limiting e armazenamento temporário)

## Como Executar

### 1. Pré-Requisitos
Dependências do Projeto
* Java 21 ou superior
* Maven
* MySQL
* Redis (opcional, recomendado)
* Variáveis de ambiente configuradas

### 2. Configuração do Banco de Dados
Para execução do sistema, deve-se configurar um usuário e um banco de dados e associá-los ao sistema por meio das variáveis `DATABASE_URL`, `DATABASE_USER` e `DATABASE_PASSWORD`.

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

### 2.1 Configuração do HTTPS
Para executar o sistema de forma segura, configure uma chave TLS por meio das variáveis `SSL_KEYSTORE_PATH` e `SSL_KEYSTORE_PASSWORD`.

* Windows
```bash
set SSL_KEYSTORE_PATH=C:\keystore.p12
set SSL_KEYSTORE_PASSWORD=senha_forte
```

* Linux / Mac
```bash
export SSL_KEYSTORE_PATH=/etc/ssl/echoes/keystore.p12
export SSL_KEYSTORE_PASSWORD=senha_forte
```

### 2.2 Proteção da Tabela de Auditoria (Audit Log)

O sistema possui uma tabela `audit_log` que registra todas as ações importantes dos usuários. Esta tabela é protegida contra alterações e exclusões em dois níveis:

- **Nível de aplicação**: A entidade `AuditLog` possui `@Immutable` e `@PreRemove`, impedindo que o Hibernate gere `UPDATE` ou `DELETE`.
- **Nível de banco de dados**: Triggers MySQL rejeitam qualquer `UPDATE` ou `DELETE` diretamente no banco.

Para ativar a proteção no banco, execute como **usuário root** no MySQL:

```bash
docker exec -it echoes_db mysql -u root -p
```

```sql
SET GLOBAL log_bin_trust_function_creators = 1;
USE echoes;
CREATE TRIGGER audit_log_no_update
BEFORE UPDATE ON audit_log
FOR EACH ROW
SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'AuditLog entries cannot be modified';

CREATE TRIGGER audit_log_no_delete
BEFORE DELETE ON audit_log
FOR EACH ROW
SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'AuditLog entries cannot be deleted';
```

Após criar os triggers, qualquer tentativa de modificar ou excluir registros de auditoria retornará erro.

> **Nota:** O `SET GLOBAL log_bin_trust_function_creators = 1` é necessário apenas uma vez. Sem ele, o MySQL exige privilégio `SUPER` para criar triggers quando o binary logging está ativo.

### 3. Configuração das demais variáveis
Além do banco de dados, é necessário configurar:

* `JWT_SECRET` → emissão de tokens  
* `API_KEY` → serviço de notificação  
* `MAIL_USER` e `MAIL_PASS` → envio de e-mail  

* Windows
```bash
set JWT_SECRET=my-secret
set API_KEY=api-key
set MAIL_USER=email
set MAIL_PASS=senha
```

* Linux / Mac
```bash
export JWT_SECRET=my-secret
export API_KEY=api-key
export MAIL_USER=email
export MAIL_PASS=senha
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

### Docker Compose

O projeto suporta SSL opcional via variáveis de ambiente:

```bash
# Sem SSL (porta 8080)
USE_SSL=false docker-compose up

# Com SSL (porta 443)
USE_SSL=true docker-compose up

# Porta customizada com SSL
USE_SSL=true SERVER_PORT=8443 APP_PORT=8443 docker-compose up
```

Variáveis disponíveis:
| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `USE_SSL` | `false` | Habilita HTTPS (true/false) |
| `SERVER_PORT` | `443` | Porta do servidor dentro do container |
| `APP_PORT` | `443` | Porta exposta no host |

A aplicação executa na porta 443 por padrão quando `USE_SSL=true`, ou 8080 quando `USE_SSL=false`.

Para usar SSL (`USE_SSL=true`), configure também:
```bash
SSL_KEYSTORE_PASSWORD=sua_senha
SSL_ALIAS=alias_do_certificado
```

# Endpoints

## Autenticação

### Login
```request
POST /api/auth/login
```
```json
{
    "email": "your@example.com",
    "password": "password"
}
```

### MFA
```request
POST /api/auth/login/2fa
```
```json
{
    "email": "your@example.com",
    "code": "code"
}
```

### Logout
```request
GET /api/auth/logout
```
Headers:
```
Authorization: Bearer $token
```

### Validar Token
```request
GET /api/auth/validate-token
```
Headers:
```
Authorization: Bearer $token
```

## Cadastro

### Register
```request
POST /api/auth/register
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
POST /api/auth/register/2fa
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
GET /api/users/me
```
Headers:
```
Authorization: Bearer $token
```

# Segurança

O sistema implementa:

* Hash seguro de senha (ex: BCrypt)
* Autenticação via JWT
* Autenticação multifator (MFA) por e-mail
* Rate limiting (proteção contra brute force)
* Suporte a HTTPS
* Auditoria imutável com triggers de banco de dados

# Observações

* Não versionar credenciais no código
* Utilizar variáveis de ambiente (.env)
* Utilizar HTTPS em produção
* Evitar exposição de tokens em múltiplos locais
* Desativar logs sensíveis em produção

# Próximos Passos

* Controle de sessão com refresh token
* Bloqueio de login por tentativas
* Integração com dispositivos IoT
* CRUD de cenários clínicos
* Testes automatizados
 
