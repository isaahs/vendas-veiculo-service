package br.com.fiap.sout.vendas.application.ports.in;

import br.com.fiap.sout.vendas.domain.model.ItemCatalogo;

public interface SincronizarItemCatalogoPort {
    void sincronizar(ItemCatalogo itemCatalogo);
}
