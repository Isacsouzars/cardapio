package com.cardapio;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

// O Spring cria save/findAll/findById/delete sozinho. O metodo abaixo vira SQL pelo nome.
public interface PratoRepository extends JpaRepository<Prato, Long> {
    List<Prato> findByDisponivelTrue();
}
