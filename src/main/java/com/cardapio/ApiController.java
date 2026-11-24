package com.cardapio;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final PratoRepository pratos;
    private final PedidoRepository pedidos;

    @Value("${app.admin-token}") String adminToken;
    @Value("${app.whatsapp}") String whatsapp;
    @Value("${app.restaurante}") String restaurante;
    @Value("${app.uploads}") String uploads;

    public ApiController(PratoRepository pratos, PedidoRepository pedidos) {
        this.pratos = pratos;
        this.pedidos = pedidos;
    }

    // ---------- PUBLICO (cliente na mesa) ----------
    record ItemPedido(Long pratoId, int qtd) {}
    record PedidoRequest(String mesa, String obs, List<ItemPedido> itens) {}

    @GetMapping("/config")
    Map<String, String> config() { return Map.of("restaurante", restaurante); }

    @GetMapping("/pratos")
    List<Prato> cardapio() { return pratos.findByDisponivelTrue(); }

    @PostMapping("/pedidos")
    Map<String, Object> criarPedido(@RequestBody PedidoRequest req) {
        if (req.itens() == null || req.itens().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pedido vazio");
        BigDecimal total = BigDecimal.ZERO;
        StringBuilder txt = new StringBuilder();
        for (ItemPedido i : req.itens()) {
            if (i.qtd() < 1 || i.qtd() > 50)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantidade invalida");
            Long pratoId = i.pratoId();
            if (pratoId == null)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Prato invalido");
            // O preco vem SEMPRE do banco, nunca do navegador (o cliente poderia adulterar).
            Prato p = pratos.findById(pratoId).filter(x -> x.disponivel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Prato invalido"));
            total = total.add(p.preco.multiply(BigDecimal.valueOf(i.qtd())));
            txt.append(i.qtd()).append("x ").append(p.nome).append("\n");
        }
        Pedido ped = new Pedido();
        ped.mesa = limitar(req.mesa(), 20);
        ped.obs = limitar(req.obs(), 300);
        ped.itens = txt.toString().trim();
        ped.total = total;
        pedidos.save(ped);

        String msg = "Pedido #" + ped.id + " - Mesa " + ped.mesa + "\n" + ped.itens
            + "\nTotal: R$ " + total + (ped.obs.isEmpty() ? "" : "\nObs: " + ped.obs);
        String link = "https://wa.me/" + whatsapp + "?text=" + URLEncoder.encode(msg, StandardCharsets.UTF_8);
        return Map.of("id", ped.id, "total", total, "whatsapp", link);
    }

    // ---------- ADMIN (painel) - exige o header X-Admin-Token ----------
    @GetMapping("/admin/pratos")
    List<Prato> todos(@RequestHeader("X-Admin-Token") String t) { auth(t); return pratos.findAll(); }

    // Com id = atualiza; sem id = cria.
    @PostMapping("/admin/pratos")
    Prato salvar(@RequestHeader("X-Admin-Token") String t, @RequestBody(required = true) Prato p) {
        auth(t);
        if (p == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Prato invalido");
        return pratos.save(p);
    }

    @DeleteMapping("/admin/pratos/{id}")
    void apagar(@RequestHeader("X-Admin-Token") String t, @PathVariable long id) { auth(t); pratos.deleteById(id); }

    @GetMapping("/admin/pedidos")
    List<Pedido> listarPedidos(@RequestHeader("X-Admin-Token") String t) {
        auth(t);
        return pedidos.findAllByOrderByCriadoEmDesc();
    }

    @PostMapping("/admin/pedidos/{id}/status")
    Pedido mudarStatus(@RequestHeader("X-Admin-Token") String t, @PathVariable long id, @RequestParam String valor) {
        auth(t);
        if (!List.of("NOVO", "PREPARANDO", "PRONTO", "ENTREGUE").contains(valor))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status invalido");
        Pedido p = pedidos.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        p.status = valor;
        return pedidos.save(p);
    }

    // Envia .glb / .usdz; devolve a URL publica (ex.: /1699-pizza.glb)
    @PostMapping("/admin/upload")
    Map<String, String> upload(@RequestHeader("X-Admin-Token") String t, @RequestParam MultipartFile arquivo) throws IOException {
        auth(t);
        String original = Objects.requireNonNullElse(arquivo.getOriginalFilename(), "modelo").toLowerCase();
        if (!original.endsWith(".glb") && !original.endsWith(".usdz"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use .glb ou .usdz");
        String nome = System.currentTimeMillis() + "-" + original.replaceAll("[^a-z0-9._-]", "_");
        Path dir = Objects.requireNonNull(Path.of(uploads));
        Files.createDirectories(dir);
        Path destino = Objects.requireNonNull(dir.resolve(nome).toAbsolutePath().normalize());
        arquivo.transferTo(destino);
        return Map.of("url", "/" + nome);
    }

    private void auth(String token) {
        if (!adminToken.equals(token)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }

    private static String limitar(String s, int max) {
        if (s == null) return "";
        s = s.trim();
        return s.length() > max ? s.substring(0, max) : s;
    }
}
