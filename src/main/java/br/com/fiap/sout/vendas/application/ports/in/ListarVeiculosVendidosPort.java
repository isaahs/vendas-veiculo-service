package br.com.fiap.sout.vendas.application.ports.in;

import br.com.fiap.sout.vendas.domain.model.ItemCatalogo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListarVeiculosVendidosPort {
    Page<ItemCatalogo> listarVendidos(Pageable pageable);
}
