package br.com.fiap.sout.vendas.adapter.out.persistence;

import br.com.fiap.sout.vendas.domain.enums.StatusItemCatalogo;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tb_itens_catalogo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCatalogoEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "veiculo_id", nullable = false)
    private UUID veiculoId;

    @Column(name = "marca", nullable = false)
    private String marca;

    @Column(name = "modelo", nullable = false)
    private String modelo;

    @Column(name = "ano", nullable = false)
    private Integer ano;

    @Column(name = "cor", nullable = false)
    private String cor;

    @Column(name = "preco", nullable = false, precision = 19, scale = 2)
    private BigDecimal preco;

    @Column(name = "placa", nullable = false, unique = true, length = 8)
    private String placa;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusItemCatalogo status;
}
