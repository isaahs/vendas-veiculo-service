package br.com.fiap.sout.vendas.adapter.in.web.mapper;

import br.com.fiap.sout.vendas.adapter.in.web.dto.ItemCatalogoResponseDto;
import br.com.fiap.sout.vendas.domain.model.ItemCatalogo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ItemCatalogoWebMapper {
    ItemCatalogoResponseDto toResponseDto(ItemCatalogo domain);
}
