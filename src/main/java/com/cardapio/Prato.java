package com.cardapio;

import jakarta.persistence.*;
import java.math.BigDecimal;

// @Entity = esta classe vira uma tabela no banco. Cada campo vira uma coluna.
// Campos publicos so para o MVP ficar curto; depois troque por getters/setters ou Lombok.
@Entity
public class Prato {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String nome;
    @Column(length = 500)
    public String descricao;
    public BigDecimal preco;
    public String categoria;
    public String modeloGlb;   // Android + navegador (arquivo .glb)
    public String modeloUsdz;  // iPhone (arquivo .usdz)
    public boolean disponivel = true;
}
