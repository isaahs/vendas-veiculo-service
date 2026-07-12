package br.com.fiap.sout.vendas.adapter.out.persistence;

import br.com.fiap.sout.vendas.domain.enums.StatusVenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VendaJpaRepository extends JpaRepository<VendaEntity, UUID> {

    Optional<VendaEntity> findByCodigoPagamento(String codigoPagamento);

    List<VendaEntity> findByStatusAndExpiraEmBefore(StatusVenda status, LocalDateTime agora);

    @Modifying
    @Query("UPDATE VendaEntity v SET v.status = :novoStatus WHERE v.codigoPagamento = :codigoPagamento AND v.status = br.com.fiap.sout.vendas.domain.enums.StatusVenda.PENDENTE_PAGAMENTO")
    int atualizarStatusSePendente(@Param("codigoPagamento") String codigoPagamento, @Param("novoStatus") StatusVenda novoStatus);
}
