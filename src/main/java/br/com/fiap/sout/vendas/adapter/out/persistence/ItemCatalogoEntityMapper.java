package br.com.fiap.sout.vendas.adapter.out.persistence;

import br.com.fiap.sout.vendas.domain.model.ItemCatalogo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ItemCatalogoEntityMapper {

    ItemCatalogo toDomain(ItemCatalogoEntity entity);

    ItemCatalogoEntity toEntity(ItemCatalogo domain);
}
