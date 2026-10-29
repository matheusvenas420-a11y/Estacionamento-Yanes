
package com.smartpark.controller;

import com.smartpark.model.Reserva;
import com.smartpark.service.ReservaService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping
    public ResponseEntity<Reserva> cadastrar(
            @RequestBody Reserva reserva)
            throws ExecutionException, InterruptedException {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(reservaService.cadastrarReserva(reserva));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping
    public List<Reserva> listar()
            throws ExecutionException, InterruptedException {
        return reservaService.listarReservas();
    }

    @GetMapping("/disponibilidade/estacionamento/{estacionamentoId}")
    public Map<String, Object> consultarDisponibilidade(
            @PathVariable String estacionamentoId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime inicio,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fim)
            throws ExecutionException, InterruptedException {
        try {
            return reservaService.consultarDisponibilidade(
                    estacionamentoId, inicio, fim);
        } catch (IllegalArgumentException e) {
            HttpStatus status = e.getMessage().contains("não encontrado")
                    ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

            throw new ResponseStatusException(status, e.getMessage());
        }
    }

    @GetMapping("/estacionamento/{estacionamentoId}/dia/{data}")
    public List<Reserva> listarPorEstacionamentoEDia(
            @PathVariable String estacionamentoId,
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate data)
            throws ExecutionException, InterruptedException {
        try {
            return reservaService.listarPorEstacionamentoEDia(
                    estacionamentoId, data);
        } catch (IllegalArgumentException e) {
            HttpStatus status = e.getMessage().contains("não encontrado")
                    ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

            throw new ResponseStatusException(status, e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Reserva buscar(@PathVariable String id)
            throws ExecutionException, InterruptedException {
        try {
            return reservaService.buscarReserva(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Reserva editar(
            @PathVariable String id,
            @RequestBody Reserva reserva)
            throws ExecutionException, InterruptedException {
        try {
            return reservaService.editarReserva(id, reserva);
        } catch (IllegalArgumentException e) {
            HttpStatus status = e.getMessage().contains("não encontrado")
                    ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

            throw new ResponseStatusException(status, e.getMessage());
        }
    }

    @PutMapping("/{id}/cancelar")
    public Reserva cancelar(@PathVariable String id)
            throws ExecutionException, InterruptedException {
        try {
            return reservaService.cancelarReserva(id);
        } catch (IllegalArgumentException e) {
            HttpStatus status = e.getMessage().contains("não encontrada")
                    ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

            throw new ResponseStatusException(status, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable String id)
            throws ExecutionException, InterruptedException {
        try {
            reservaService.cancelarReserva(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            HttpStatus status = e.getMessage().contains("não encontrada")
                    ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

            throw new ResponseStatusException(status, e.getMessage());
        }
    }
}
