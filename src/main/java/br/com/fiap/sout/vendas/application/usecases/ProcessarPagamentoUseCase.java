package br.com.fiap.sout.vendas.application.usecases;

import br.com.fiap.sout.vendas.application.ports.in.ProcessarPagamentoPort;
import br.com.fiap.sout.vendas.application.ports.out.ItemCatalogoRepositoryPort;
import br.com.fiap.sout.vendas.application.ports.out.VendaRepositoryPort;
import br.com.fiap.sout.vendas.domain.enums.StatusPagamento;
import br.com.fiap.sout.vendas.domain.enums.StatusVenda;
import br.com.fiap.sout.vendas.domain.exceptions.VendaNaoEncontradaException;
import br.com.fiap.sout.vendas.domain.model.Venda;
import java.util.Optional;

public class ProcessarPagamentoUseCase implements ProcessarPagamentoPort {

    private final VendaRepositoryPort vendaRepositoryPort;
    private final ItemCatalogoRepositoryPort itemCatalogoRepositoryPort;

    public ProcessarPagamentoUseCase(
            VendaRepositoryPort vendaRepositoryPort,
            ItemCatalogoRepositoryPort itemCatalogoRepositoryPort
    ) {
        this.vendaRepositoryPort = vendaRepositoryPort;
        this.itemCatalogoRepositoryPort = itemCatalogoRepositoryPort;
    }

    @Override
    public void processarPagamento(String codigoPagamento, StatusPagamento status) {
        StatusVenda novoStatus = (status == StatusPagamento.APROVADO) ? StatusVenda.CONFIRMADA : StatusVenda.CANCELADA;

        Optional<Venda> vendaAtualizadaOpt = vendaRepositoryPort.atualizarStatusSePendente(codigoPagamento, novoStatus);

        if (vendaAtualizadaOpt.isPresent()) {
            Venda venda = vendaAtualizadaOpt.get();
            if (novoStatus == StatusVenda.CONFIRMADA) {
                itemCatalogoRepositoryPort.marcarComoVendido(venda.itemCatalogoId());
            } else {
                itemCatalogoRepositoryPort.marcarComoDisponivel(venda.itemCatalogoId());
            }
        } else {
            Optional<Venda> existenteOpt = vendaRepositoryPort.buscarPorCodigoPagamento(codigoPagamento);
            if (existenteOpt.isEmpty()) {
                throw new VendaNaoEncontradaException("Venda com código de pagamento " + codigoPagamento + " não encontrada.");
            }
        }
    }
}
