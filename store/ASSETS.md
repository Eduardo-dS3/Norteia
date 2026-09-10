# Materiais gráficos da loja — especificações

Tudo em RGB, sem transparência onde a Play exige (ícone e destaque).

## Ícone da loja
- **512 × 512 px**, PNG de 32 bits.
- Reaproveite o conceito do ícone do app (`app/src/main/res/drawable/ic_launcher_*`):
  estrela de 4 pontas com a ponta Norte vermelha, fundo grafite com leve aurora.
- Sem texto no ícone. Margem de segurança de ~1/12 nas bordas.

## Gráfico de destaque (feature graphic)
- **1024 × 500 px**, PNG ou JPG, sem canal alfa.
- Fundo preto/grafite com a rosa dos ventos à esquerda e o nome "Norteia" +
  tagline curta ("Bússola e GPS. Offline.") à direita.
- Nada de mockup de celular com moldura de marca (evita parecer plágio).

## Screenshots de celular (mín. 2, ideal 4–8)
- **1080 × 1920 px** (retrato), PNG/JPG.
- Capturar no app real:
  1. Tela cheia com a bússola + coordenadas + satélites.
  2. Card de coordenadas mostrando UTM/MGRS.
  3. Sol/Lua e nível de bolha visíveis.
  4. Aviso de calibração (opcional).
- Pode adicionar uma faixa de legenda no topo (fundo sólido + texto curto).
  Não use frases de efeito não verificáveis ("o mais preciso do mundo").

## Screenshots de tablet (opcional, recomendado p/ alcance)
- 7": 1200 × 1920 ; 10": 1600 × 2560 (ou proporções equivalentes).

## Vídeo (opcional)
- Link do YouTube, 30–120 s, mostrando a agulha reagindo e a troca de formato.

## Onde colocar os arquivos
Sugestão de pasta local (fora do controle de versão se forem pesados):
`store/graphics/` com `icon-512.png`, `feature-1024x500.png`, `phone-01.png`…
