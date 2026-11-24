package com.cardapio;

import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CardapioApplication {
    public static void main(String[] args) {
        SpringApplication.run(CardapioApplication.class, args);
    }

    // Roda ao iniciar: cria um prato de exemplo se o banco estiver vazio (para testar o AR).
    @Bean
    CommandLineRunner seed(PratoRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                Prato p = new Prato();
                p.nome = "Prato de exemplo (astronauta)";
                p.descricao = "Modelo de teste. Troque por um prato escaneado no painel.";
                p.preco = new BigDecimal("29.90");
                p.categoria = "Pratos";
                p.modeloGlb = "https://modelviewer.dev/shared-assets/models/Astronaut.glb";
                p.modeloUsdz = "https://modelviewer.dev/shared-assets/models/Astronaut.usdz";
                repo.save(p);
            }
        };
    }
}
