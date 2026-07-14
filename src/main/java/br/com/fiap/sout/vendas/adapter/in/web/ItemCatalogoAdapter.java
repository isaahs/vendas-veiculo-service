package br.com.fiap.sout.vendas.adapter.in.web;

import br.com.fiap.sout.vendas.adapter.in.web.dto.ItemCatalogoResponseDto;
import br.com.fiap.sout.vendas.adapter.in.web.dto.SincronizarVeiculoRequestDto;
import br.com.fiap.sout.vendas.adapter.in.web.mapper.ItemCatalogoWebMapper;
import br.com.fiap.sout.vendas.application.ports.in.ListarVeiculosDisponiveisPort;
import br.com.fiap.sout.vendas.application.ports.in.ListarVeiculosVendidosPort;
import br.com.fiap.sout.vendas.application.ports.in.SincronizarItemCatalogoPort;
import br.com.fiap.sout.vendas.application.ports.in.VerificarVeiculoVendidoPort;
import br.com.fiap.sout.vendas.domain.enums.StatusItemCatalogo;
import br.com.fiap.sout.vendas.domain.model.ItemCatalogo;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping
@Tag(name = "Catálogo")
public class ItemCatalogoAdapter {

    private final SincronizarItemCatalogoPort sincronizarItemCatalogoPort;
    private final ListarVeiculosDisponiveisPort listarVeiculosDisponiveisPort;
    private final ListarVeiculosVendidosPort listarVeiculosVendidosPort;
    private final VerificarVeiculoVendidoPort verificarVeiculoVendidoPort;
    private final ItemCatalogoWebMapper webMapper;

    public ItemCatalogoAdapter(
            SincronizarItemCatalogoPort sincronizarItemCatalogoPort,
            ListarVeiculosDisponiveisPort listarVeiculosDisponiveisPort,
            ListarVeiculosVendidosPort listarVeiculosVendidosPort,
            VerificarVeiculoVendidoPort verificarVeiculoVendidoPort,
            ItemCatalogoWebMapper webMapper
    ) {
        this.sincronizarItemCatalogoPort = sincronizarItemCatalogoPort;
        this.listarVeiculosDisponiveisPort = listarVeiculosDisponiveisPort;
        this.listarVeiculosVendidosPort = listarVeiculosVendidosPort;
        this.verificarVeiculoVendidoPort = verificarVeiculoVendidoPort;
        this.webMapper = webMapper;
    }

    @PostMapping("/veiculos")
    @SecurityRequirement(name = "bearerAuth")
    @Hidden
    public ResponseEntity<Void> sincronizar(@Valid @RequestBody SincronizarVeiculoRequestDto request) {
        ItemCatalogo itemCatalogo = new ItemCatalogo(
                UUID.randomUUID(),
                request.veiculoId(),
                request.marca(),
                request.modelo(),
                request.ano(),
                request.cor(),
                request.preco(),
                request.placa(),
                StatusItemCatalogo.DISPONIVEL
        );
        sincronizarItemCatalogoPort.sincronizar(itemCatalogo);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/veiculos")
    public ResponseEntity<Page<ItemCatalogoResponseDto>> listarDisponiveis(
            @ParameterObject @PageableDefault(sort = "preco", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<ItemCatalogoResponseDto> response = listarVeiculosDisponiveisPort.listarDisponiveis(pageable)
                .map(webMapper::toResponseDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/veiculos/vendidos")
    public ResponseEntity<Page<ItemCatalogoResponseDto>> listarVendidos(
            @ParameterObject @PageableDefault(sort = "preco", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<ItemCatalogoResponseDto> response = listarVeiculosVendidosPort.listarVendidos(pageable)
                .map(webMapper::toResponseDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/veiculos/{id}/vendido")
    @SecurityRequirement(name = "bearerAuth")
    @Hidden
    public ResponseEntity<Boolean> verificarVendido(@PathVariable("id") UUID veiculoId) {
        boolean vendido = verificarVeiculoVendidoPort.verificarVeiculoVendido(veiculoId);
        return ResponseEntity.ok(vendido);
    }
}
