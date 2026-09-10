# Formulário "Segurança dos dados" da Play — respostas (v1, sem anúncios)

Preencha exatamente assim enquanto o app **não** tiver SDK de anúncios nem
analytics. Guarde este arquivo para reusar e para revisar quando ligar o AdMob.

## Coleta e compartilhamento

| Pergunta | Resposta |
|---|---|
| Seu app coleta ou compartilha algum dos tipos de dados exigidos? | **Não** |
| Justificativa | A localização e os sensores são usados só em tempo de execução, no aparelho, para exibir informação ao usuário. Nada é enviado pela rede (o app nem tem permissão de internet), nada é gravado, nada vai para terceiros. Preferências locais (formato de coordenada, toggles) não são "dados pessoais" nem identificadores. |
| Todos os dados em trânsito são criptografados? | N/A (não há transmissão) |
| O usuário pode pedir exclusão de dados? | N/A (não há dados coletados). O usuário pode revogar a permissão ou desinstalar. |

## Práticas de segurança

- Dados criptografados em trânsito: **N/A / Sim** (não há tráfego).
- Mecanismo de exclusão: **N/A**.
- Compromisso com a Política de Famílias: o app **não** é destinado a crianças.

## Permissões declaradas

| Permissão | Motivo (para o texto de revisão da Play) |
|---|---|
| `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` | Mostrar coordenadas, altitude, velocidade, precisão, satélites e posição de Sol/Lua, **apenas em primeiro plano**. |

Não há: internet, armazenamento, câmera, contatos, telefone, `ACCESS_BACKGROUND_LOCATION`,
`FOREGROUND_SERVICE`, `RECEIVE_BOOT_COMPLETED`, nem permissões restritas.

---

## Quando o AdMob entrar (v1.1+)

Mudará para **"Sim, coleta dados"**:

| Tipo de dado | Coletado | Compartilhado | Finalidade |
|---|---|---|---|
| ID do dispositivo ou de outros | Sim | Sim | Publicidade / marketing |
| Histórico de uso do app (interações) | Sim | Sim | Publicidade; Análise |
| Localização aproximada | Possível (AdMob) | Possível | Publicidade |
| Info de diagnóstico / falhas | Sim | Sim | Análise |

- "Dados criptografados em trânsito": **Sim**.
- Consentimento via **UMP** (GDPR/EEA/UK) e aviso LGPD para o Brasil.
- Atualizar a Política de Privacidade (seção 4) **antes** de publicar a versão.
- Confirme as categorias exatas no painel do AdMob (elas mudam de tempos em tempos).
