package br.com.fiap.sout.vendas.application.ports.in;

import br.com.fiap.sout.vendas.domain.model.ItemCatalogo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListarVeiculosDisponiveisPort {
    Page<ItemCatalogo> listarDisponiveis(Pageable pageable);
}
