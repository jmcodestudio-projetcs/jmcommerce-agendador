# Contratos de API — Agendamento para Fotógrafos

Versão de 12/11/2026.

## Convenções

A API é REST com JSON, em dois grupos: `/painel/**` exige login do fotógrafo e `/publico/**` é aberto para o cliente final, que não tem conta.

| Item | Regra |
| --- | --- |
| Base | `/api/v1` |
| Autenticação | JWT no header `Authorization: Bearer <token>`, só em `/painel/**` |
| Identificação do fotógrafo | Sempre pelo token; nunca por id na URL do painel |
| Datas e horas | `data` em `YYYY-MM-DD`, `hora` em `HH:mm`, no fuso `America/Sao_Paulo` (fixo na v1) |
| Timestamps | ISO-8601, ex.: `2026-11-12T10:15:00-03:00` |
| IDs | Numéricos (`Long`) |
| Dinheiro | Decimal em reais, ex.: `350.00` |
| Paginação | Nenhuma na v1; listas de agenda e agendamentos são filtradas por período (`de`, `ate`) |

Todo erro devolve o mesmo corpo:

```json
{
  "codigo": "HORARIO_INDISPONIVEL",
  "mensagem": "Este horário acabou de ser reservado. Escolha outro.",
  "campos": { "email": "E-mail inválido" }
}
```

`campos` só aparece em erro de validação.

| HTTP | Quando |
| --- | --- |
| 400 | Validação de campos (`VALIDACAO`) |
| 401 | Token ausente, inválido ou login incorreto (`NAO_AUTENTICADO`) |
| 403 | Conta suspensa por inadimplência (`CONTA_SUSPENSA`) |
| 404 | Recurso não existe ou não pertence ao fotógrafo (`NAO_ENCONTRADO`) |
| 409 | Conflito de regra: `HORARIO_INDISPONIVEL`, `SLUG_EM_USO`, `EMAIL_EM_USO`, `TRANSICAO_INVALIDA`, `HORARIO_COM_AGENDAMENTO` |

## Autenticação e conta do fotógrafo

Só o fotógrafo tem login; cadastro e login devolvem o token e os dados da conta.

| Método | Rota | Corpo | Resposta |
| --- | --- | --- | --- |
| POST | `/auth/cadastro` | `nome`, `email`, `senha`, `slug` | 201 `{ token, fotografo }` |
| POST | `/auth/login` | `email`, `senha` | 200 `{ token, fotografo }` |
| POST | `/auth/esqueci-senha` | `email` | 204 sempre, exista ou não o e-mail |
| POST | `/auth/redefinir-senha` | `token`, `novaSenha` | 204 |
| GET | `/painel/conta` | — | 200 `fotografo` |
| PUT | `/painel/conta` | `nome`, `slug`, `telefoneWhatsapp`, `bio` | 200 `fotografo` |

Objeto `fotografo`:

```json
{
  "id": 1,
  "nome": "Fabio Seabra Fotografia",
  "email": "contato@exemplo.com",
  "slug": "fabio-seabra",
  "telefoneWhatsapp": "5511999990000",
  "bio": "Ensaios externos e em estúdio.",
  "statusAssinatura": "TESTE",
  "testeAte": "2026-12-12"
}
```

- `slug` aceita apenas letras minúsculas, números e hífen, e é único (409 `SLUG_EM_USO`).
- `senha` tem no mínimo 8 caracteres.
- `statusAssinatura` é `TESTE`, `ATIVA` ou `SUSPENSA`; o cadastro começa em `TESTE` com 30 dias.
- O token vale 7 dias, sem refresh token; ao expirar, o fotógrafo faz login de novo.

## Serviços

CRUD simples dos serviços que o fotógrafo oferece; o preço é opcional e ele decide se aparece na página pública.

| Método | Rota | Corpo | Resposta |
| --- | --- | --- | --- |
| GET | `/painel/servicos` | — | 200 lista de `servico` |
| POST | `/painel/servicos` | `servico` sem `id` | 201 `servico` |
| PUT | `/painel/servicos/{id}` | `servico` sem `id` | 200 `servico` |
| DELETE | `/painel/servicos/{id}` | — | 204 |

Objeto `servico`:

```json
{
  "id": 10,
  "nome": "Ensaio gestante",
  "descricao": "Ensaio externo, 30 fotos tratadas.",
  "duracaoMinutos": 90,
  "preco": 350.00,
  "exibirPreco": true,
  "ativo": true
}
```

- `nome` e `duracaoMinutos` são obrigatórios; `preco` pode ser `null`.
- `DELETE` apaga o serviço; se já houver agendamento ligado a ele, apenas marca `ativo: false`.
- Serviço inativo não aparece na página pública.

## Agenda (horários disponíveis)

O fotógrafo abre a agenda manualmente por período, e cada horário aberto vira uma linha que comporta uma única sessão.

| Método | Rota | Corpo ou filtro | Resposta |
| --- | --- | --- | --- |
| GET | `/painel/horarios` | `?de=2026-12-01&ate=2026-12-31` | 200 lista de `horario` |
| POST | `/painel/horarios` | abertura por período (abaixo) | 201 `{ "criados": 16 }` |
| DELETE | `/painel/horarios/{id}` | — | 204, ou 409 `HORARIO_COM_AGENDAMENTO` |

Corpo do `POST`:

```json
{
  "dataInicio": "2026-12-01",
  "dataFim": "2026-12-31",
  "diasSemana": [2, 4, 6],
  "horas": ["09:00", "14:00"]
}
```

Objeto `horario`:

```json
{ "id": 501, "data": "2026-12-02", "hora": "09:00", "ocupado": false }
```

- `diasSemana` usa 1 = segunda até 7 = domingo.
- O `POST` cria uma linha para cada combinação de dia e hora; as que já existem são ignoradas, sem erro.
- Para abrir um único horário, basta `dataInicio` igual a `dataFim` e uma hora.
- `ocupado` é `true` quando há agendamento pendente ou aprovado naquele horário.
- A duração do serviço é só informativa: o sistema não calcula sobreposição, e é o fotógrafo quem espaça os horários que abre.

## Página pública (cliente sem login)

O cliente entra pelo link do fotógrafo, escolhe serviço e horário, informa seus dados e acompanha o pedido por um código enviado por e-mail.

| Método | Rota | Corpo ou filtro | Resposta |
| --- | --- | --- | --- |
| GET | `/publico/{slug}` | — | 200 perfil com serviços ativos |
| GET | `/publico/{slug}/horarios` | `?de=&ate=` | 200 lista de horários livres |
| POST | `/publico/{slug}/agendamentos` | pedido (abaixo) | 201 `agendamentoPublico`, ou 409 `HORARIO_INDISPONIVEL` |
| GET | `/publico/agendamentos/{codigo}` | — | 200 `agendamentoPublico` |
| POST | `/publico/agendamentos/{codigo}/cancelar` | — | 200 `agendamentoPublico` |

Resposta do perfil:

```json
{
  "nome": "Fabio Seabra Fotografia",
  "bio": "Ensaios externos e em estúdio.",
  "telefoneWhatsapp": "5511999990000",
  "servicos": [
    { "id": 10, "nome": "Ensaio gestante", "descricao": "...", "duracaoMinutos": 90, "preco": 350.00 }
  ]
}
```

Corpo do pedido:

```json
{
  "servicoId": 10,
  "horarioId": 501,
  "nome": "Maria Souza",
  "email": "maria@exemplo.com",
  "telefone": "11988887777",
  "observacao": "Ensaio no parque, se possível."
}
```

Objeto `agendamentoPublico`:

```json
{
  "codigo": "k3J9xQ2mT7aB",
  "status": "PENDENTE",
  "servico": "Ensaio gestante",
  "data": "2026-12-02",
  "hora": "09:00",
  "fotografo": "Fabio Seabra Fotografia",
  "telefoneWhatsapp": "5511999990000"
}
```

- `preco` só vem no perfil quando o serviço tem `exibirPreco: true`.
- `servicoId`, `horarioId`, `nome` e `email` são obrigatórios; `telefone` e `observacao` são opcionais.
- A lista de horários traz apenas os livres e futuros, no formato `{ id, data, hora }`.
- `codigo` é um texto aleatório longo, gerado no backend; ele substitui o login do cliente e vai no link do e-mail.
- Slug inexistente ou conta suspensa respondem 404 nas rotas com `{slug}`.
- O link de WhatsApp (`wa.me`) é montado no frontend a partir de `telefoneWhatsapp`.

## Painel de agendamentos

O fotógrafo lista os pedidos e decide cada um; toda ação é um `POST` próprio, sem `PUT` genérico de status.

| Método | Rota | Corpo ou filtro | Resposta |
| --- | --- | --- | --- |
| GET | `/painel/agendamentos` | `?status=PENDENTE&de=&ate=` | 200 lista de `agendamento` |
| GET | `/painel/agendamentos/{id}` | — | 200 `agendamento` |
| POST | `/painel/agendamentos/{id}/aprovar` | — | 200 `agendamento` |
| POST | `/painel/agendamentos/{id}/recusar` | `motivo` (opcional) | 200 `agendamento` |
| POST | `/painel/agendamentos/{id}/cancelar` | `motivo` (opcional) | 200 `agendamento` |
| POST | `/painel/agendamentos/{id}/reagendar` | `horarioId` | 200 `agendamento`, ou 409 `HORARIO_INDISPONIVEL` |

Objeto `agendamento`:

```json
{
  "id": 9001,
  "codigo": "k3J9xQ2mT7aB",
  "status": "PENDENTE",
  "servico": { "id": 10, "nome": "Ensaio gestante" },
  "horario": { "id": 501, "data": "2026-12-02", "hora": "09:00" },
  "cliente": { "nome": "Maria Souza", "email": "maria@exemplo.com", "telefone": "11988887777" },
  "observacao": "Ensaio no parque, se possível.",
  "motivo": null,
  "criadoEm": "2026-11-12T10:15:00-03:00"
}
```

- Todos os filtros do `GET` são opcionais; sem filtro, a lista traz os agendamentos de hoje em diante, ordenados por data e hora.
- `reagendar` troca o horário e mantém o status atual; o horário antigo volta a ficar livre.
- Ação fora da ordem permitida responde 409 `TRANSICAO_INVALIDA`.

## Regras de status e conflito de horário

Um horário aceita no máximo um agendamento ativo (pendente ou aprovado), e quem garante isso é o banco, não o código.

| Ação | Quem | De | Para | E-mail automático |
| --- | --- | --- | --- | --- |
| Criar pedido | Cliente | — | `PENDENTE` | Cliente e fotógrafo |
| Aprovar | Fotógrafo | `PENDENTE` | `APROVADO` | Cliente |
| Recusar | Fotógrafo | `PENDENTE` | `RECUSADO` | Cliente |
| Reagendar | Fotógrafo | `PENDENTE` ou `APROVADO` | mesmo status | Cliente |
| Cancelar | Fotógrafo | `PENDENTE` ou `APROVADO` | `CANCELADO` | Cliente |
| Cancelar | Cliente | `PENDENTE` ou `APROVADO` | `CANCELADO` | Fotógrafo |

- O pedido pendente já ocupa o horário; ele só volta a ficar livre com recusa, cancelamento ou reagendamento.
- A tabela `agendamento` tem um índice único em `horario_id` restrito aos status `PENDENTE` e `APROVADO`.
- Se dois clientes enviarem o mesmo horário ao mesmo tempo, o segundo `INSERT` falha no índice e a API responde 409 `HORARIO_INDISPONIVEL`.
- O frontend trata o 409 recarregando a lista de horários livres e pedindo outra escolha.
- Não há lock, fila nem reserva temporária de horário.

## Fora desta versão dos contratos

Estes pontos entram depois, quando chegarmos à etapa correspondente:

- Assinatura e gateway de pagamento: checkout, webhook e mudança de `statusAssinatura`.
- Push PWA: endpoint para registrar a inscrição do navegador do fotógrafo.
- Sinal por Pix, que está no backlog.
- Upload de foto ou logo do fotógrafo.
