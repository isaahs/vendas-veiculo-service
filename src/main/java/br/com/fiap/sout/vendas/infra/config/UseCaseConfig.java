package br.com.fiap.sout.vendas.infra.config;

import br.com.fiap.sout.vendas.application.ports.in.*;
import br.com.fiap.sout.vendas.application.ports.out.ItemCatalogoRepositoryPort;
import br.com.fiap.sout.vendas.application.ports.out.VendaRepositoryPort;
import br.com.fiap.sout.vendas.application.usecases.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class UseCaseConfig {

    @Value("${reserva.expiracao.minutos:10}")
    private int reservaExpiracaoMinutos;

    @Bean
    public SincronizarItemCatalogoPort sincronizarItemCatalogoPort(
            ItemCatalogoRepositoryPort itemCatalogoRepositoryPort
    ) {
        return new SincronizarItemCatalogoUseCase(itemCatalogoRepositoryPort);
    }

    @Bean
    public ListarVeiculosDisponiveisPort listarVeiculosDisponiveisPort(
            ItemCatalogoRepositoryPort itemCatalogoRepositoryPort
    ) {
        return new ListarVeiculosDisponiveisUseCase(itemCatalogoRepositoryPort);
    }

    @Bean
    public ListarVeiculosVendidosPort listarVeiculosVendidosPort(
            ItemCatalogoRepositoryPort itemCatalogoRepositoryPort
    ) {
        return new ListarVeiculosVendidosUseCase(itemCatalogoRepositoryPort);
    }

    @Bean
    public EfetuarVendaPort efetuarVendaPort(
            ItemCatalogoRepositoryPort itemCatalogoRepositoryPort,
            VendaRepositoryPort vendaRepositoryPort
    ) {
        return new EfetuarVendaUseCase(itemCatalogoRepositoryPort, vendaRepositoryPort, reservaExpiracaoMinutos);
    }

    @Bean
    public ProcessarPagamentoPort processarPagamentoPort(
            VendaRepositoryPort vendaRepositoryPort,
            ItemCatalogoRepositoryPort itemCatalogoRepositoryPort
    ) {
        return new ProcessarPagamentoUseCase(vendaRepositoryPort, itemCatalogoRepositoryPort);
    }

    @Bean
    public VerificarVeiculoVendidoPort verificarVeiculoVendidoPort(
            ItemCatalogoRepositoryPort itemCatalogoRepositoryPort
    ) {
        return new VerificarVeiculoVendidoUseCase(itemCatalogoRepositoryPort);
    }

    @Bean
    public ExpirarReservasPort expirarReservasPort(
            VendaRepositoryPort vendaRepositoryPort,
            ItemCatalogoRepositoryPort itemCatalogoRepositoryPort
    ) {
        return new ExpirarReservasUseCase(vendaRepositoryPort, itemCatalogoRepositoryPort);
    }
}
