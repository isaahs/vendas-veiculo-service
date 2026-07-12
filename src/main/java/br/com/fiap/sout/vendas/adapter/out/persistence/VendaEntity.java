package br.com.fiap.sout.vendas.adapter.out.persistence;

import br.com.fiap.sout.vendas.domain.enums.StatusVenda;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_vendas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "item_catalogo_id", nullable = false)
    private UUID itemCatalogoId;

    @Column(name = "cpf_comprador", nullable = false)
    private String cpfComprador;

    @Column(name = "data_venda", nullable = false)
    private LocalDateTime dataVenda;

    @Column(name = "codigo_pagamento", nullable = false, unique = true)
    private String codigoPagamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusVenda status;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;
}
