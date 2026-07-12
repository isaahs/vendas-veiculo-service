package br.com.fiap.sout.vendas.application.ports.out;

import br.com.fiap.sout.vendas.domain.enums.StatusVenda;
import br.com.fiap.sout.vendas.domain.model.Venda;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VendaRepositoryPort {
    Venda salvar(Venda venda);
    Optional<Venda> buscarPorCodigoPagamento(String codigoPagamento);
    List<Venda> buscarVendasExpiradasPendentes(LocalDateTime agora);
    Optional<Venda> atualizarStatusSePendente(String codigoPagamento, StatusVenda novoStatus);
}
