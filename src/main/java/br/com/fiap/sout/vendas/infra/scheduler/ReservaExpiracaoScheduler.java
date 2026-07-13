package br.com.fiap.sout.vendas.infra.scheduler;

import br.com.fiap.sout.vendas.application.ports.in.ExpirarReservasPort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservaExpiracaoScheduler {

    private final ExpirarReservasPort expirarReservasPort;

    public ReservaExpiracaoScheduler(ExpirarReservasPort expirarReservasPort) {
        this.expirarReservasPort = expirarReservasPort;
    }

    @Scheduled(fixedDelayString = "${reserva.expiracao.intervalo-ms:60000}")
    public void expirarReservas() {
        expirarReservasPort.expirarReservas();
    }
}
