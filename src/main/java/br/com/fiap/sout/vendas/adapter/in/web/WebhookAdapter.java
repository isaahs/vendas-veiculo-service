package br.com.fiap.sout.vendas.adapter.in.web;

import br.com.fiap.sout.vendas.adapter.in.web.dto.WebhookPagamentoRequestDto;
import br.com.fiap.sout.vendas.application.ports.in.ProcessarPagamentoPort;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pagamentos/webhook")
@Tag(name = "Pagamentos")
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
