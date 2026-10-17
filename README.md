# Norteia

Bússola + GPS de tela única para Android. Nativo, offline, sem cadastro, sem
rastreamento.

<p align="center"><em>Kotlin · Jetpack Compose · Material 3 · min SDK 26 · target SDK 36</em></p>

---

## O que já está pronto (v1.0.0)

| Área | Detalhe |
|---|---|
| **Bússola** | Sensor `ROTATION_VECTOR` fundido, suavização na circunferência (sem "pulo" no 0/360), correção de **declinação magnética** (modelo embutido no Android) com alternância Norte Verdadeiro / Magnético, aviso de calibração e de inclinação. |
| **GPS** | `FusedLocationProvider` em alta precisão: latitude/longitude, **erro horizontal em metros**, altitude, velocidade, curso. Detecção de localização simulada. |
| **Satélites** | `GnssStatus` ao vivo: satélites em uso / visíveis e C/N0 (força de sinal). |
| **Coordenadas** | Graus decimais, GMS, **UTM** e **MGRS** — conversões próprias, testadas contra referências publicadas. Copiar, compartilhar e abrir em qualquer app de mapas (`geo:`). |
| **Sol e Lua** | Azimute e elevação, azimute de nascer/pôr do Sol, fase da Lua. Cálculo astronômico local, offline. |
| **Nível** | Nível de bolha pelo vetor gravidade. |
| **Visual** | Tema escuro AMOLED com acento "aurora", rosa dos ventos desenhada 100% em vetor no `Canvas` (nada de imagem de terceiros), animações suaves, háptico nos cardeais. |
| **Espaço de anúncio** | Faixa fixa de 56 dp presa na base, fora da área de toque — descrita em `docs/MONETIZATION.md`. **A v1 não tem anúncios nem SDK de anúncios.** |
| **Privacidade** | Sem permissão de internet. Sem `ACCESS_BACKGROUND_LOCATION`. Nada sai do aparelho. |

## Estrutura

```
app/src/main/java/io/github/eduardods3/norteia/
├── core/            # lógica pura, testável na JVM (sem Android)
│   ├── math/        # AngleMath — normalização e suavização de ângulos
│   ├── geo/         # LatLon, DMS, UTM/MGRS, formatos, pontos cardeais
│   └── astro/       # SunMoon — posição de Sol e Lua
├── data/
│   ├── location/    # FusedLocationSource, GnssStatusSource
│   ├── sensors/     # CompassSource, LevelSource
│   └── settings/    # SettingsRepository (DataStore)
├── di/              # AppContainer (service locator manual, sem Hilt)
└── ui/              # MainViewModel + CompassScreen + components/ + theme/
```

Tudo flui por `Flow`s frios combinados no `MainViewModel` e coletados com
`WhileSubscribed`, então GPS e sensores só ligam com a tela visível.

## Como abrir e compilar

Esta máquina **não** tem o ambiente Android (só Java 8). Faça assim:

1. Instale o **Android Studio** (versão 2026.x ou mais recente). Ele traz o
   próprio JDK 17+.
2. `File > Open` → pasta do projeto. Deixe o Gradle sincronizar; se ele
   sugerir gerar o `gradle-wrapper.jar` ou os scripts `gradlew`, aceite.
3. Se o sync reclamar de versão de AGP/Gradle/Compose, use o **AGP Upgrade
   Assistant** ou aceite os valores que o Studio sugerir — as versões em
   `gradle/libs.versions.toml` são um ponto de partida conhecido.
4. `Run` num emulador ou aparelho. Sensores de bússola **não funcionam no
   emulador** — teste GPS/coordenadas no emulador e a bússola num aparelho real.

Pelo terminal (depois de ter JDK 17+ e o Android SDK):

```bash
./gradlew testDebugUnitTest      # roda os testes de conversão e de ângulo
./gradlew assembleDebug          # gera o APK de debug
./gradlew bundleRelease          # gera o AAB (precisa de keystore.properties)
```

## Publicação e monetização

- `docs/RELEASE_CHECKLIST.md` — passo a passo da Play Console (assinatura,
  Data safety, classificação, teste fechado de 12 pessoas por 14 dias, etc.).
- `docs/MONETIZATION.md` — como ligar o banner do AdMob depois, com consentimento
  (UMP) e sem infringir política.
- `docs/DATA_SAFETY.md` — respostas prontas do formulário "Segurança dos dados".
- `store/` — textos da ficha da loja em pt-BR e en-US e especificações dos
  materiais gráficos.
- Site + política de privacidade: pasta `docs/` (GitHub Pages).

## Licença

Código do app: proprietário, © Eduardo. Veja `LICENSE`.
Bibliotecas de terceiros: Apache 2.0 (lista na tela "Licenças" do app).
