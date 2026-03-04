# Introdução

Servidor de autenticação para aplicação mobile e sistema IoT. O projeto visa estabelecer e implementar sistema de autenticação de usuários e gestão de credenciais seguindo boas normas e conformidade com a Lei Geral de Proteção de Dados (LGPD). 

## End Points

### Registro de Usuários 
* `POST /api/auth/register`

*Body*
```json
{
    "name": "user@example.com",
    "password": "strong password",
    "role": "STUDENT"
}
```

*Response*
* 200 OK - Usuário criado com sucesso.
* 400 BAD REQUEST - Usuário já existente no sistema.


### Login
* `POST /api/auth/login`

*Body*
```json
{
    "name": "user@example.com",
    "password": "strong password"
}
```

*Response*
* 200 OK - Usuário criado com sucesso.
```json
{
    "token": "token"
}
```

* 400 BAD REQUEST - Usuário ou senha inválidos.