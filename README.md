# 🔐 Sistema de Autenticação com Microsserviços

Sistema distribuído de autenticação de usuários utilizando arquitetura de microsserviços com Spring Boot, Node.js, RabbitMQ e envio de e-mails.


## 🏗 Arquitetura
┌─────────────┐ ┌──────────────┐ ┌─────────────┐
│ Frontend │────▶│ User Service │────▶│ RabbitMQ │
│ (Node.js) │ │ (Porta 8081) │ │ CloudAMQP │
│ (Porta 3000)│◀────│ │ └──────┬──────┘
└─────────────┘ └──────────────┘ │
┌────▼──────┐
│ Email │
│ Service │
│ (Porta 8082)│
└───────────┘

text

### Microsserviços:
1. **Frontend (Node.js + Express)** - Porta 3000
   - Interface do usuário
   - Proxy para o backend
   
2. **User Service (Spring Boot)** - Porta 8081
   - Gerenciamento de usuários
   - Autenticação JWT
   - Comunicação com RabbitMQ
   
3. **Email Service (Spring Boot)** - Porta 8082
   - Consumidor RabbitMQ
   - Envio de e-mails via Gmail SMTP

---

## 🚀 Tecnologias

### Backend
- Java 17
- Spring Boot 3.5.14
- Spring Security
- Spring Data JPA
- JWT (Auth0)
- RabbitMQ (CloudAMQP)
- MySQL 8.0
- Maven

### Frontend
- Node.js
- Express
- Axios
- HTML5/CSS3/JavaScript

---

## 📦 Pré-requisitos

### Software necessário:
- [JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- [Node.js 18+](https://nodejs.org/)
- [MySQL 8.0](https://dev.mysql.com/downloads/installer/)
- [Maven](https://maven.apache.org/download.cgi)
- [Git](https://git-scm.com/downloads)

### Contas necessárias:
- Conta [CloudAMQP](https://www.cloudamqp.com/) (RabbitMQ)
- Conta Gmail com [Senha de App](https://support.google.com/accounts/answer/185833)

---

## ⚙ Configuração

### 1. Banco de Dados MySQL

```sql
CREATE DATABASE ms_user;
CREATE DATABASE ms_email;
2. Variáveis de Ambiente (application.properties)
User Service (user/src/main/resources/application.properties):

properties
server.port=8081
spring.application.name=user

spring.datasource.url=jdbc:mysql://localhost:3306/ms_user?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=SUA_SENHA

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

spring.rabbitmq.addresses=amqps://SEU_USUARIO:SUA_SENHA@jackal.rmq.cloudamqp.com/SEU_VHOST
broker.queue.email.name=default.email
Email Service (email/src/main/resources/application.properties):

properties
server.port=8082
spring.application.name=email

spring.datasource.url=jdbc:mysql://localhost:3306/ms_email?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=SUA_SENHA

spring.jpa.hibernate.ddl-auto=update

spring.rabbitmq.addresses=amqps://SEU_USUARIO:SUA_SENHA@jackal.rmq.cloudamqp.com/SEU_VHOST
broker.queue.email.name=default.email

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=SEU_EMAIL@gmail.com
spring.mail.password=SUA_SENHA_APP_GMAIL
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
3. Instalar dependências do Frontend
bash
cd auth-frontend
npm install
🎯 Como Executar
Ordem de inicialização:
Inicie o MySQL

Inicie o RabbitMQ (CloudAMQP)

Inicie o Email Service:

bash
cd email
mvn spring-boot:run
Inicie o User Service:

bash
cd user
mvn spring-boot:run
Inicie o Frontend:

bash
cd auth-frontend
npm start
Ou use o script de inicialização (Windows):
powershell
# iniciar.ps1
Write-Host "Iniciando Email Service..." -ForegroundColor Green
Start-Process powershell -ArgumentList "cd email; mvn spring-boot:run"

Start-Sleep -Seconds 5

Write-Host "Iniciando User Service..." -ForegroundColor Green
Start-Process powershell -ArgumentList "cd user; mvn spring-boot:run"

Start-Sleep -Seconds 5

Write-Host "Iniciando Frontend..." -ForegroundColor Green
Start-Process powershell -ArgumentList "cd auth-frontend; npm start"

Write-Host "`nSistema iniciado!" -ForegroundColor Cyan
Write-Host "Frontend: http://localhost:3000" -ForegroundColor Cyan
🔄 Fluxo do Sistema
Solicitação de Código

Usuário digita e-mail

Sistema cria usuário temporário (se não existir)

Publica mensagem no RabbitMQ

Email Service consome e envia código de 6 dígitos

Validação

Usuário digita código recebido

Sistema valida e gera token JWT

Cadastro de Perfil

Usuário preenche nome completo

Escolhe cargo (Cliente/Administrador)

Dados são salvos via endpoint autenticado

Dashboard

Botão "Testar endpoint protegido" - verifica acesso

Botão "Meu perfil" - exibe dados do usuário

Botão "Sair" - remove token e redireciona

📡 Endpoints
User Service (8081)
Método	Endpoint	Descrição	Autenticação
POST	/auth/request-code	Solicitar código	Não
POST	/auth/verify-code	Validar código	Não
POST	/users/update-profile	Atualizar perfil	Sim
GET	/users/me	Dados do usuário	Sim
GET	/users/test/customer	Teste Customer	Sim (CUSTOMER)
Frontend (3000)
Rota	Descrição
GET /	Tela inicial (e-mail)
GET /verify?email=	Tela de verificação
GET /register	Tela de cadastro
GET /dashboard	Dashboard protegido
📸 Capturas de Tela
Tela Inicial - Solicitar Código
prints/tela_inicial.png

E-mail com Código
prints\email.jpeg

Tela de Verificação
prints\tela_verificacao.png

Tela de Cadastro
prints\formularii_dado.png

Dashboard - Teste Endpoint Protegido
prints\acesso_dasshbototokenteste.png

Dashboard - Meu Perfil
prints\meu_perfil.png

Banco de Dados - Tabelas
prints\bd.png
prints\bd2.png


🔒 Segurança
Senhas criptografadas com BCrypt

Autenticação via JWT (expira em 4 horas)

Rotas protegidas por roles (CUSTOMER/ADMINISTRATOR)

Senha de app Gmail (não usa senha real)

CORS configurado para segurança