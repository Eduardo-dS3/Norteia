# Checklist de publicação — Norteia

Ordem prática para tirar o app do zero até a produção na Google Play, sem cair
em política. Datas e regras conferidas em setembro de 2026.

---

## 0. Antes de tudo: identidade do app

- [ ] **`applicationId`**: hoje está `io.github.eduardods3.norteia`
  (`app/build.gradle.kts`). Ele é **permanente** depois do primeiro envio.
  Se quiser outro (ex.: um domínio próprio `br.com.seudominio.norteia`), troque
  **agora**: `namespace` + `applicationId` no `app/build.gradle.kts`, a pasta
  `app/src/main/java/io/github/eduardods3/norteia/` e os `package` no topo dos
  `.kt` (o Android Studio faz tudo junto em *Refactor > Rename* no pacote).
- [ ] Nome de exibição: `Norteia` (`app_name` em `res/values/strings.xml`).

## 0.5. Antes de tudo (parte 2): risco jurídico — o que é real e o que não é

Isto não é aconselhamento jurídico, só o retrato honesto do que dá pra saber
sem advogado:

- **Custo certo**: a taxa única de **US$ 25** da conta Google Play (item 1).
  Não existe outra cobrança obrigatória — o app não tem servidor, API paga nem
  anúncios na v1.
- **"Posso ser processado?"**: risco **baixo, mas não zero**, como em qualquer
  app publicado. O que reduz o risco, já no repositório:
  - Nenhum dado pessoal coletado/transmitido (`docs/privacy-policy.html`).
  - `docs/terms-of-use.html` deixa claro que o Norteia é uma ferramenta de
    referência, não um instrumento certificado de navegação, e limita
    responsabilidade por decisões tomadas com base nele (nos limites do CDC).
  - Permissões mínimas e justificadas (só localização em primeiro plano).
  - Código e arte 100% originais (sem assets de terceiros plagiados).
- [x] E-mail de contato definido: `norteiabussola@gmail.com` (já atualizado em
  `privacy-policy.html`, `terms-of-use.html` e `store/listing-pt-BR.md`).
- [ ] **Conferir o nome "Norteia"** na busca de marcas do INPI
  (https://busca.inpi.gov.br/pePI/) antes de investir em marketing pesado —
  uma busca geral não encontrou conflito, mas só o INPI é autoritativo.
- [ ] Ler o **Developer Distribution Agreement** do Google ao criar a conta —
  é um contrato que só você pode aceitar (sua conta, sua assinatura).

## 1. Conta Google Play Console

- [ ] Criar conta de desenvolvedor (**US$ 25**, uma vez) e concluir a
  **verificação de identidade** (documento + endereço). Pode levar alguns dias.
- [ ] Conta **pessoal criada depois de 13/11/2023** cai na regra de **teste
  fechado com no mínimo 12 testadores por 14 dias seguidos** antes de liberar
  produção. Comece a juntar gente desde já (amigos, grupos, e-mails).

## 2. Assinatura

- [ ] Ativar **Play App Signing** (padrão para apps novos).
- [ ] Gerar a **upload key** local:
  ```
  keytool -genkeypair -v -keystore norteia-upload.jks -alias norteia \
    -keyalg RSA -keysize 4096 -validity 10000
  ```
- [ ] Criar `keystore.properties` na raiz do projeto (já está no `.gitignore`):
  ```
  storeFile=../norteia-upload.jks
  storePassword=...
  keyAlias=norteia
  keyPassword=...
  ```
  Com esse arquivo presente, `./gradlew bundleRelease` assina sozinho.
- [ ] Guardar `norteia-upload.jks` e as senhas em lugar seguro (perdeu = dor de cabeça).

## 3. Build de produção

- [ ] `targetSdk = 36` (obrigatório para apps/atualizações novas desde 31/08/2026). ✔ já configurado.
- [ ] `versionCode` inteiro crescente a cada envio; `versionName` legível (`1.0.0`).
- [ ] `./gradlew testDebugUnitTest` passa.
- [ ] `./gradlew bundleRelease` gera `app/build/outputs/bundle/release/app-release.aab`.
- [ ] Testar o AAB via **bundletool** ou faixa interna antes de subir para teste fechado.
- [ ] Conferir que o `minify`/`shrinkResources` não quebrou nada (rodar o app release).

## 4. Ficha da loja (Presença na loja)

- [ ] Título (≤ 30), descrição curta (≤ 80), descrição completa (≤ 4000) — textos em `store/`.
- [ ] **Ícone 512×512 PNG**, **gráfico de destaque 1024×500**, **mín. 2 screenshots** de celular
  (1080×1920 recomendado). Specs em `store/ASSETS.md`.
- [ ] Idiomas: `pt-BR` (padrão) e `en-US`.
- [ ] Categoria: *Ferramentas* (Tools). Tags: bússola, GPS, navegação.
- [ ] **URL da Política de Privacidade**: `https://eduardo-ds3.github.io/Norteia/privacy-policy.html`
  (publique a pasta `docs/` no GitHub Pages: *Settings > Pages > Deploy from a branch > main /docs*).
- [ ] **Termos de Uso** (opcional na ficha, mas linkado dentro do app): `https://eduardo-ds3.github.io/Norteia/terms-of-use.html`.
- [x] E-mails de contato já trocados — ver item 0.5.

## 5. Conteúdo do app (aba obrigatória)

- [ ] **Segurança dos dados**: respostas prontas em `docs/DATA_SAFETY.md`
  (resumo: "nenhum dado coletado ou compartilhado" enquanto não houver anúncios).
- [ ] **Classificação de conteúdo**: responder o questionário — app utilitário,
  sem conteúdo sensível → deve sair "Livre / Everyone".
- [ ] **Público-alvo e conteúdo**: faixa **18+ / não voltado a crianças**
  (fica fora da *Families Policy* e simplifica tudo).
- [ ] **Anúncios**: declarar **"Não, meu app não contém anúncios"** na v1.
  (Ao ligar o AdMob depois, voltar aqui e mudar para "Sim".)
- [ ] **Permissões**: nenhuma permissão sensível/restrita é usada. Não é preciso
  formulário de permissão de localização em segundo plano (o app não usa).
- [ ] App de governo? Não. App de notícias? Não. App financeiro? Não.

## 6. Lançamento em etapas

- [ ] **Teste interno** (você + poucos) → smoke test.
- [ ] **Teste fechado** com ≥ 12 testadores, mantê-los inscritos ≥ 14 dias.
- [ ] Rodar o **Pre-launch report** (a Play instala e testa em vários aparelhos —
  ótimo para pegar crash de sensor/permissão).
- [ ] Solicitar **acesso à produção** no painel; responder as perguntas do teste fechado.
- [ ] Produção com **lançamento gradual** (ex.: 20% → 50% → 100%), acompanhando ANRs/crashes.

## 7. Pós-lançamento

- [ ] Monitorar **Android vitals** (crash rate, ANR, wakelocks) e as avaliações.
- [ ] Responder avaliações (melhora a nota e a conversão).
- [ ] Só depois de estável, planejar a v1.1 com o banner — ver `docs/MONETIZATION.md`.
