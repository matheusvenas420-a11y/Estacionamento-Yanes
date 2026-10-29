
package com.smartpark.service;

import com.smartpark.model.Estacionamento;
import com.smartpark.repository.EstacionamentoFirebaseRepository;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.springframework.stereotype.Service;

@Service
public class EstacionamentoService {

    private final EstacionamentoFirebaseRepository repository;

    public EstacionamentoService(
            EstacionamentoFirebaseRepository repository) {
        this.repository = repository;
    }

    public String verificarStatus() {
        return "SMARTPARK API funcionando com Firebase!";
    }

    public List<Estacionamento> listarEstacionamentos()
            throws ExecutionException, InterruptedException {
        return repository.listarTodos();
    }

    public Estacionamento cadastrarEstacionamento(
            Estacionamento estacionamento)
            throws ExecutionException, InterruptedException {

        estacionamento.setId(null);
        validarEstacionamento(estacionamento);

        return repository.salvar(estacionamento);
    }

    public Estacionamento editarEstacionamento(String id,
            Estacionamento estacionamento)
            throws ExecutionException, InterruptedException {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "O ID do estacionamento é obrigatório.");
        }

        validarEstacionamento(estacionamento);

        Estacionamento atualizado =
                repository.atualizar(id, estacionamento);

        if (atualizado == null) {
            throw new IllegalArgumentException(
                    "Estacionamento não encontrado.");
        }

        return atualizado;
    }

    public void excluirEstacionamento(String id)
            throws ExecutionException, InterruptedException {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "O ID do estacionamento é obrigatório.");
        }

        boolean excluido = repository.excluir(id);

        if (!excluido) {
            throw new IllegalArgumentException(
                    "Estacionamento não encontrado.");
        }
    }

    private void validarEstacionamento(
            Estacionamento estacionamento) {

        if (estacionamento.getNome() == null
                || estacionamento.getNome().isBlank()) {
            throw new IllegalArgumentException(
                    "O nome do estacionamento é obrigatório.");
        }

        if (estacionamento.getEndereco() == null
                || estacionamento.getEndereco().isBlank()) {
            throw new IllegalArgumentException(
                    "O endereço do estacionamento é obrigatório.");
        }

        if (estacionamento.getVagasTotais() < 0
                || estacionamento.getVagasDisponiveis() < 0
                || estacionamento.getVagasDisponiveis()
                > estacionamento.getVagasTotais()) {
            throw new IllegalArgumentException(
                    "A quantidade de vagas é inválida.");
        }
    }
}
