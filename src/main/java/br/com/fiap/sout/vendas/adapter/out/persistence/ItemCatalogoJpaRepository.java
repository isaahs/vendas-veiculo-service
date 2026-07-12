package br.com.fiap.sout.vendas.adapter.out.persistence;

import br.com.fiap.sout.vendas.domain.enums.StatusItemCatalogo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface ItemCatalogoJpaRepository extends JpaRepository<ItemCatalogoEntity, UUID> {

    Optional<ItemCatalogoEntity> findByVeiculoId(UUID veiculoId);

    Page<ItemCatalogoEntity> findByStatusOrderByPrecoAsc(StatusItemCatalogo status, Pageable pageable);

    @Modifying
    @Query("UPDATE ItemCatalogoEntity i SET i.status = br.com.fiap.sout.vendas.domain.enums.StatusItemCatalogo.RESERVADO WHERE i.id = :id AND i.status = br.com.fiap.sout.vendas.domain.enums.StatusItemCatalogo.DISPONIVEL")
    int reservar(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE ItemCatalogoEntity i SET i.status = br.com.fiap.sout.vendas.domain.enums.StatusItemCatalogo.VENDIDO WHERE i.id = :id")
    void marcarComoVendido(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE ItemCatalogoEntity i SET i.status = br.com.fiap.sout.vendas.domain.enums.StatusItemCatalogo.DISPONIVEL WHERE i.id = :id")
    void marcarComoDisponivel(@Param("id") UUID id);
}
