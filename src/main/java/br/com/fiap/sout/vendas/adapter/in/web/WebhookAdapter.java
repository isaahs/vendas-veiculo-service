package br.com.fiap.sout.vendas.adapter.in.web;

import br.com.fiap.sout.vendas.adapter.in.web.dto.WebhookPagamentoRequestDto;
import br.com.fiap.sout.vendas.application.ports.in.ProcessarPagamentoPort;
import br.com.fiap.sout.vendas.domain.enums.StatusPagamento;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pagamentos/webhook")
public class WebhookAdapter {

    private final ProcessarPagamentoPort processarPagamentoPort;

    public WebhookAdapter(ProcessarPagamentoPort processarPagamentoPort) {
        this.processarPagamentoPort = processarPagamentoPort;
    }

    @PostMapping
    @SecurityRequirement(name = "hmacAuth")
    public ResponseEntity<Void> processarPagamento(@Valid @RequestBody WebhookPagamentoRequestDto request) {
        processarPagamentoPort.processarPagamento(request.codigoPagamento(), request.status());
        return ResponseEntity.ok().build();
    }
}
