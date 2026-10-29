
package com.smartpark.service;

import java.util.concurrent.ExecutionException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservaExpiracaoScheduler {

    private final ReservaService reservaService;

    public ReservaExpiracaoScheduler(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @Scheduled(fixedDelay = 60000)
    public void verificarReservasExpiradas() {
        try {
            reservaService.expirarReservasPendentes();
        } catch (ExecutionException | InterruptedException e) {
            System.err.println(
                    "Erro ao verificar reservas expiradas: "
                    + e.getMessage());

            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
