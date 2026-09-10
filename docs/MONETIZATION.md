# Monetização — ligar o banner depois (com segurança)

A v1 vai para a loja **sem anúncios e sem SDK de anúncios** — isso mantém o
formulário "Segurança dos dados" limpo ("nenhum dado coletado"), a nota alta e o
app leve. Quando quiser ligar a receita, siga este roteiro.

## Estratégia recomendada

- **Um único banner adaptativo**, ancorado na base, dentro do `AdSlot`
  (`ui/components/AdSlot.kt`) — o espaço de 56 dp já está reservado fora da área
  de toque da bússola, então **nada muda de lugar** ao ativar.
- **Sem interstitial, sem rewarded, sem anúncio na abertura.** É um app de
  "olhar 3 segundos e fechar": tela cheia de anúncio irrita e gera clique
  acidental (motivo comum de suspensão de conta AdMob).
- Opcional no futuro: uma compra única "remover anúncios" via Play Billing.

## Passo a passo

### 1. Dependências (`gradle/libs.versions.toml` + `app/build.gradle.kts`)
```kotlin
implementation("com.google.android.gms:play-services-ads:24.6.0")   // versão atual à época
implementation("com.google.android.ump:user-messaging-platform:3.2.0")
```

### 2. Manifesto (`app/src/main/AndroidManifest.xml`)
```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<!-- dentro de <application> -->
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY"/>
```

### 3. Consentimento + init (`NorteiaApp.onCreate()`)
```kotlin
val params = ConsentRequestParameters.Builder().build()
val consentInfo = UserMessagingPlatform.getConsentInformation(this)
consentInfo.requestConsentInfoUpdate(currentActivity, params, {
    UserMessagingPlatform.loadAndShowConsentFormIfRequired(currentActivity) {
        if (consentInfo.canRequestAds()) MobileAds.initialize(this) {}
    }
}, { /* erro: seguir sem anúncios */ })
```
> O UMP é o CMP do próprio Google e atende GDPR/EEA + Reino Unido; para o
> Brasil (LGPD) você também pode habilitar o aviso. Configure o formulário em
> **AdMob > Privacidade e mensagens**.

### 4. Trocar o corpo do `AdSlot`
Substituir o `Text("NORTEIA")` por:
```kotlin
AndroidView(
    modifier = Modifier.fillMaxWidth().heightIn(min = AD_SLOT_HEIGHT),
    factory = { ctx ->
        AdView(ctx).apply {
            adUnitId = BuildConfig.AD_UNIT_BANNER   // teste vs. produção via buildType
            setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(ctx, /*widthDp*/ 360))
            loadAd(AdRequest.Builder().build())
        }
    },
)
```
Manter `AD_SLOT_HEIGHT` como altura mínima para o layout não "pular" enquanto carrega.

### 5. IDs de anúncio
- Desenvolvimento: usar **sempre** os IDs de teste do AdMob
  (`ca-app-pub-3940256099942544/6300978111` para banner).
- Produção: criar o bloco real e injetar via `buildConfigField` por `buildType`,
  nunca commitar em texto puro se preferir.

### 6. `app-ads.txt`
- Depois de ter a conta AdMob, preencher `docs/app-ads.txt` com o ID de editor e
  **hospedar na raiz** do domínio que você declarar como site do desenvolvedor
  na Play (ver observações no próprio arquivo).

### 7. Declarações da Play Console
- **Conteúdo do app > Anúncios**: mudar para "Sim, contém anúncios".
- **Segurança dos dados**: atualizar — o SDK de anúncios coleta, no mínimo,
  identificadores do dispositivo e dados aproximados de uso. Marcar as categorias
  que o AdMob informar e ligar "dados criptografados em trânsito".
- **Política de Privacidade**: publicar a seção de anúncios (o texto já está
  previsto em `docs/privacy-policy.html`, seção 4) **antes** de subir a versão.
- Classificação de conteúdo: refazer o questionário marcando presença de anúncios.

## Regras de ouro para não ser suspenso

- Nunca peça para clicar no anúncio, nem posicione controles colados nele.
- Um banner por tela. Nada de anúncio sobre conteúdo ou cobrindo botão.
- Não recarregar o banner em intervalo curto demais (respeitar ~30–60 s).
- Não clicar nos próprios anúncios em produção (use IDs de teste no debug).
