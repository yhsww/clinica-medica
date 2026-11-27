# como rodar o projeto

## precisa de:

- **JDK de sua preferência (>21)**.
- **bun** ou **node** para o frontend.

## execução rápida

na raiz do projeto:

| sistema | comando    |
| ------- | ---------- |
| Linux   | `./run.sh` |
| Windows | `run.bat`  |

## execução manual

### back

```bash
cd backend
./mvnw spring-boot:run  # ou `mvnw.cmd spring-boot:run` no Windows
```

a api fica em `http://localhost:8080`.

### front

#### bun

```bash
cd frontend
bun install
bun run dev
```

#### node

```bash
cd frontend
npm install
npm run dev
```

o app fica em `http://localhost:5173` (ou `http://localhost:5174` caso a porta já esteja ocupada).

---

- UI: `http://localhost:5173`
- API: `http://localhost:8080/api`

### usuários de teste

| E-mail                 | Senha      |
| ---------------------- | ---------- |
| `admin@clinica.com`    | `admin`    |
| `medico@clinica.com`   | `senha123` |
| `paciente@clinica.com` | `senha123` |

### autenticação JWT

`POST /api/auth/login` (e `/api/auth/cadastrar`) devolve um campo `token`.

### endpoints da API

| Método                 | Rota                           | Descrição                                     |
| ---------------------- | ------------------------------ | --------------------------------------------- |
| `POST`                 | `/api/auth/login`              | autentica e devolve o token JWT               |
| `POST`                 | `/api/auth/cadastrar`          | cadastra as credenciais de paciente ou médico |
| `GET`                  | `/api/medicos`                 | lista médicos                                 |
| `GET`, `PUT`, `DELETE` | `/api/medicos/{id}`            | busca, edita ou exclui médico                 |
| `GET`                  | `/api/pacientes`               | lista pacientes                               |
| `GET`, `PUT`, `DELETE` | `/api/pacientes/{id}`          | busca, edita ou exclui paciente               |
| `GET`                  | `/api/consultas`               | lista consultas                               |
| `POST`                 | `/api/consultas`               | agenda uma consulta e gera sua conta          |
| `GET`, `PUT`, `DELETE` | `/api/consultas/{id}`          | busca, edita ou exclui consulta               |
| `POST`                 | `/api/consultas/{id}/realizar` | marca uma consulta como realizada             |
| `POST`                 | `/api/consultas/{id}/cancelar` | cancela uma consulta                          |
| `GET`                  | `/api/planos`                  | lista os planos disponíveis                   |
| `GET`                  | `/api/especialidades`          | lista as especialidades disponíveis           |
| `GET`                  | `/api/contas`                  | lista contas                                  |
| `GET`, `DELETE`        | `/api/contas/{id}`             | busca ou exclui conta                         |
| `POST`                 | `/api/contas/{id}/pagar`       | marca uma conta como paga                     |
