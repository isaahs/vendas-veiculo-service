package br.com.fiap.sout.vendas.adapter.out.persistence;

import br.com.fiap.sout.vendas.domain.model.Venda;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VendaEntityMapper {

    Venda toDomain(VendaEntity entity);

    VendaEntity toEntity(Venda domain);
}
