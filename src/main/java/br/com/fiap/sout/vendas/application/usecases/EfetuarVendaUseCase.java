package br.com.fiap.sout.vendas.application.usecases;

import br.com.fiap.sout.vendas.application.ports.in.EfetuarVendaCommand;
import br.com.fiap.sout.vendas.application.ports.in.EfetuarVendaPort;
import br.com.fiap.sout.vendas.application.ports.out.ItemCatalogoRepositoryPort;
import br.com.fiap.sout.vendas.application.ports.out.VendaRepositoryPort;
import br.com.fiap.sout.vendas.domain.enums.StatusVenda;
import br.com.fiap.sout.vendas.domain.exceptions.VeiculoIndisponivelException;
import br.com.fiap.sout.vendas.domain.model.Venda;
import java.time.LocalDateTime;
import java.util.UUID;

public class EfetuarVendaUseCase implements EfetuarVendaPort {

    private final ItemCatalogoRepositoryPort itemCatalogoRepositoryPort;
    private final VendaRepositoryPort vendaRepositoryPort;
    private final int expiracaoMinutos;

    public EfetuarVendaUseCase(
            ItemCatalogoRepositoryPort itemCatalogoRepositoryPort,
            VendaRepositoryPort vendaRepositoryPort,
            int expiracaoMinutos
    ) {
        this.itemCatalogoRepositoryPort = itemCatalogoRepositoryPort;
        this.vendaRepositoryPort = vendaRepositoryPort;
        this.expiracaoMinutos = expiracaoMinutos;
    }

    @Override
    public Venda efetuarVenda(EfetuarVendaCommand command) {
        int linhasAfetadas = itemCatalogoRepositoryPort.reservar(command.itemCatalogoId());
        if (linhasAfetadas == 0) {
            throw new VeiculoIndisponivelException("O veículo não está disponível para reserva ou venda.");
        }

        UUID vendaId = UUID.randomUUID();
        String codigoPagamento = UUID.randomUUID().toString();
        LocalDateTime dataVenda = LocalDateTime.now();
        LocalDateTime expiraEm = dataVenda.plusMinutes(expiracaoMinutos);

        Venda venda = new Venda(
            vendaId,
            command.itemCatalogoId(),
            command.cpfComprador(),
            dataVenda,
            codigoPagamento,
            StatusVenda.PENDENTE_PAGAMENTO,
            expiraEm
        );

        return vendaRepositoryPort.salvar(venda);
    }
}
