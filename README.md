# Cardápio Web com AR (MVP) — Java + Spring Boot

Cliente encosta o celular na tag NFC da mesa → abre o cardápio → vê o prato em 3D/AR → pede.
O pedido cai no **painel** do restaurante e o cliente pode enviar uma cópia pelo **WhatsApp**.

## O que você precisa configurar manualmente

1. **Instalar**: JDK 17+ e Maven (ou VS Code com "Extension Pack for Java" + "Spring Boot Extension Pack").
2. **Editar `src/main/resources/application.properties`**:
   - `app.restaurante` → nome que aparece no topo.
   - `app.whatsapp` → número com país+DDD, só dígitos (ex.: `5551999999999`).
   - `app.admin-token` → **troque a senha** do painel.
3. **Rodar**: `mvn spring-boot:run` (ou botão Run no VS Code em `CardapioApplication`).
4. **Abrir**: cardápio em `http://localhost:8080/?mesa=1` · painel em `http://localhost:8080/painel.html`.
5. **HTTPS (obrigatório para AR no celular)**: o AR só funciona em endereço `https`. Para testar, use um túnel
   grátis, ex.: Cloudflare Tunnel (`cloudflared tunnel --url http://localhost:8080`) ou ngrok. Para uso real, hospede em
   um servidor/VPS com domínio e certificado (Let's Encrypt).
6. **Modelos 3D** (veja abaixo) e **tags NFC** (veja abaixo).

## Modelos 3D dos pratos (scan)

- Escaneie com apps como Scaniverse, Polycam ou KIRI Engine (confira quais formatos a versão grátis exporta).
- Exporte **.glb** (Android e navegador) e **.usdz** (iPhone). Se só tiver o .glb, o iPhone pode não abrir o AR
  (o model-viewer tenta contornar, mas a qualidade varia). Conversão: Blender ou Apple Reality Converter (Mac).
- Mantenha cada arquivo abaixo de ~10 MB (reduza polígonos/textura) para o cardápio abrir rápido em 4G.
- Mantenha a **escala real** do prato (o código usa `ar-scale="fixed"` para ele aparecer do tamanho verdadeiro).
- Envie pelo painel (**Cadastrar prato** → escolher .glb e .usdz). Os arquivos ficam na pasta `uploads/`.

## Totens/plaquinhas NFC

- Compre tags **NTAG213/215** (adesivo ou cartão).
- No celular, use o app **NFC Tools** → Gravar → Adicionar registro → URL → `https://SEU-DOMINIO/?mesa=1` → Gravar.
- Uma tag por mesa, mudando só o número (`?mesa=2`, `?mesa=3`...). Também gere um **QR code** com a mesma URL
  como plano B (iPhones antigos e celulares sem NFC).

## Como estudar o código (ordem sugerida)

1. `Prato.java` e `Pedido.java` → as tabelas do banco.
2. `PratoRepository.java` / `PedidoRepository.java` → como o Spring conversa com o banco sem você escrever SQL.
3. `ApiController.java` → todas as rotas (`/api/...`): cardápio, criar pedido, painel, upload.
4. `static/index.html` → tela do cliente (busca `/api/pratos`, monta cards, carrinho, envia pedido).
5. `static/painel.html` → tela do restaurante (atualiza pedidos a cada 5 s).

Dica: faça um pedido e acompanhe no navegador (F12 → aba Network) cada requisição até o Java.

## Fora do MVP (próximos passos)

- Login de verdade (hoje é uma senha única) e HTTPS no servidor.
- Banco PostgreSQL no lugar do H2 (arquivo local).
- Pagamento online (Pix), impressão de pedido, notificação sonora/WhatsApp automático via API oficial.
- Várias lojas, edição de prato existente pelo painel (hoje só criar/ocultar/apagar), testes automatizados.
- Hoje o botão do WhatsApp abre a conversa com o pedido pronto; o envio automático exige a API oficial do WhatsApp Business.
