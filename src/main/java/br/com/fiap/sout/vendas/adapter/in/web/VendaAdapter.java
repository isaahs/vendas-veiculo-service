package br.com.fiap.sout.vendas.adapter.in.web;

import br.com.fiap.sout.vendas.adapter.in.web.dto.EfetuarVendaRequestDto;
import br.com.fiap.sout.vendas.adapter.in.web.dto.VendaResponseDto;
import br.com.fiap.sout.vendas.adapter.in.web.mapper.VendaWebMapper;
import br.com.fiap.sout.vendas.application.ports.in.EfetuarVendaCommand;
import br.com.fiap.sout.vendas.application.ports.in.EfetuarVendaPort;
import br.com.fiap.sout.vendas.domain.model.Venda;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vendas")
@Tag(name = "Vendas")
public class VendaAdapter {

    private final EfetuarVendaPort efetuarVendaPort;
    private final VendaWebMapper webMapper;

    public VendaAdapter(EfetuarVendaPort efetuarVendaPort, VendaWebMapper webMapper) {
        this.efetuarVendaPort = efetuarVendaPort;
        this.webMapper = webMapper;
    }

    @PostMapping
    public ResponseEntity<VendaResponseDto> efetuarVenda(@Valid @RequestBody EfetuarVendaRequestDto request) {
        EfetuarVendaCommand command = new EfetuarVendaCommand(request.cpfComprador(), request.veiculoId());
        Venda venda = efetuarVendaPort.efetuarVenda(command);
        VendaResponseDto response = webMapper.toResponseDto(venda);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
