package com.cardapio;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos")
public class Pedido {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String mesa;
    @Column(length = 2000)
    public String itens;          // texto pronto: "2x Pizza\n1x Suco"
    public BigDecimal total;
    public String obs;
    public String status = "NOVO"; // NOVO -> PREPARANDO -> PRONTO -> ENTREGUE
    public LocalDateTime criadoEm = LocalDateTime.now();
}
