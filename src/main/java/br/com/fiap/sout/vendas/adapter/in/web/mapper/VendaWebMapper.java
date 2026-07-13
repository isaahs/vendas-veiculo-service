package br.com.fiap.sout.vendas.adapter.in.web.mapper;

import br.com.fiap.sout.vendas.adapter.in.web.dto.VendaResponseDto;
import br.com.fiap.sout.vendas.domain.model.Venda;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VendaWebMapper {
    VendaResponseDto toResponseDto(Venda domain);
}
