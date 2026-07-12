package br.com.fiap.sout.vendas.application.usecases;

import br.com.fiap.sout.vendas.application.ports.in.ExpirarReservasPort;
import br.com.fiap.sout.vendas.application.ports.out.ItemCatalogoRepositoryPort;
import br.com.fiap.sout.vendas.application.ports.out.VendaRepositoryPort;
import br.com.fiap.sout.vendas.domain.enums.StatusVenda;
import br.com.fiap.sout.vendas.domain.model.Venda;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ExpirarReservasUseCase implements ExpirarReservasPort {

    private final VendaRepositoryPort vendaRepositoryPort;
    private final ItemCatalogoRepositoryPort itemCatalogoRepositoryPort;

    public ExpirarReservasUseCase(
            VendaRepositoryPort vendaRepositoryPort,
            ItemCatalogoRepositoryPort itemCatalogoRepositoryPort
    ) {
        this.vendaRepositoryPort = vendaRepositoryPort;
        this.itemCatalogoRepositoryPort = itemCatalogoRepositoryPort;
    }

    @Override
    public void expirarReservas() {
        LocalDateTime agora = LocalDateTime.now();
        List<Venda> expiradas = vendaRepositoryPort.buscarVendasExpiradasPendentes(agora);

        for (Venda venda : expiradas) {
            Optional<Venda> vendaCancelada = vendaRepositoryPort.atualizarStatusSePendente(
                venda.codigoPagamento(),
                StatusVenda.CANCELADA
            );

            if (vendaCancelada.isPresent()) {
                itemCatalogoRepositoryPort.marcarComoDisponivel(venda.itemCatalogoId());
            }
        }
    }
}
