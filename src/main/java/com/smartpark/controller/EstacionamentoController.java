
package com.smartpark.controller;

import com.smartpark.model.Estacionamento;
import com.smartpark.service.EstacionamentoService;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class EstacionamentoController {

    private final EstacionamentoService estacionamentoService;

    public EstacionamentoController(
            EstacionamentoService estacionamentoService) {
        this.estacionamentoService = estacionamentoService;
    }

    @GetMapping("/status")
    public String status() {
        return estacionamentoService.verificarStatus();
    }

    @GetMapping("/estacionamentos")
    public List<Estacionamento> listarEstacionamentos()
            throws ExecutionException, InterruptedException {
        return estacionamentoService.listarEstacionamentos();
    }

    @PostMapping("/estacionamentos")
    @ResponseStatus(HttpStatus.CREATED)
    public Estacionamento cadastrarEstacionamento(
            @RequestBody Estacionamento estacionamento)
            throws ExecutionException, InterruptedException {
        try {
            return estacionamentoService.cadastrarEstacionamento(
                    estacionamento);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/estacionamentos/{id}")
    public Estacionamento editarEstacionamento(
            @PathVariable String id,
            @RequestBody Estacionamento estacionamento)
            throws ExecutionException, InterruptedException {
        try {
            return estacionamentoService.editarEstacionamento(
                    id, estacionamento);
        } catch (IllegalArgumentException e) {
            HttpStatus status = e.getMessage().contains("não encontrado")
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.BAD_REQUEST;

            throw new ResponseStatusException(status, e.getMessage());
        }
    }

    @DeleteMapping("/estacionamentos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirEstacionamento(@PathVariable String id)
            throws ExecutionException, InterruptedException {
        try {
            estacionamentoService.excluirEstacionamento(id);
        } catch (IllegalArgumentException e) {
            HttpStatus status = e.getMessage().contains("não encontrado")
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.BAD_REQUEST;

            throw new ResponseStatusException(status, e.getMessage());
        }
    }
}
