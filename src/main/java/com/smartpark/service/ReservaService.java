package com.smartpark.service;

import com.smartpark.model.Estacionamento;
import com.smartpark.model.Reserva;
import com.smartpark.repository.EstacionamentoFirebaseRepository;
import com.smartpark.repository.ReservaFirebaseRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class ReservaService {

    private static final long TEMPO_EXPIRACAO_MINUTOS = 15;

    private final ReservaFirebaseRepository reservaRepository;
    private final EstacionamentoFirebaseRepository estacionamentoRepository;

    public ReservaService(
            ReservaFirebaseRepository reservaRepository,
            EstacionamentoFirebaseRepository estacionamentoRepository) {
        this.reservaRepository = reservaRepository;
        this.estacionamentoRepository = estacionamentoRepository;
    }

    public Reserva cadastrarReserva(Reserva reserva)
            throws ExecutionException, InterruptedException {

        validarDados(reserva);
        expirarReservasPendentes();

        Estacionamento estacionamento =
                buscarEstacionamento(reserva.getEstacionamentoId());

        if (!estacionamento.isAtivo()) {
            throw new IllegalArgumentException(
                    "O estacionamento está inativo.");
        }

        String vagaLivre = encontrarVagaLivre(
                reserva, estacionamento, null);

        if (vagaLivre == null) {
            throw new IllegalArgumentException(
                    "Não existem vagas disponíveis nesse período.");
        }

        reserva.setVagaId(vagaLivre);
        reserva.setStatus("PENDENTE");
        reserva.setDataHoraExpiracao(
                LocalDateTime.now().plusMinutes(TEMPO_EXPIRACAO_MINUTOS));

        return reservaRepository.salvar(reserva);
    }

    public List<Reserva> listarReservas()
            throws ExecutionException, InterruptedException {

        expirarReservasPendentes();
        return reservaRepository.listarTodas();
    }

    public Reserva buscarReserva(String id)
            throws ExecutionException, InterruptedException {

        validarId(id);
        expirarReservasPendentes();

        Reserva reserva = reservaRepository.buscarPorId(id);

        if (reserva == null) {
            throw new IllegalArgumentException(
                    "Reserva não encontrada.");
        }

        return reserva;
    }

    public List<Reserva> listarPorEstacionamentoEDia(
            String estacionamentoId, LocalDate data)
            throws ExecutionException, InterruptedException {

        if (estacionamentoId == null || estacionamentoId.isBlank()) {
            throw new IllegalArgumentException(
                    "O estacionamento é obrigatório.");
        }

        if (data == null) {
            throw new IllegalArgumentException(
                    "A data é obrigatória.");
        }

        expirarReservasPendentes();
        buscarEstacionamento(estacionamentoId);

        return reservaRepository.listarPorEstacionamentoEDia(
                estacionamentoId, data);
    }

    public Reserva editarReserva(String id, Reserva novaReserva)
            throws ExecutionException, InterruptedException {

        validarId(id);
        validarDados(novaReserva);
        expirarReservasPendentes();

        Reserva existente = reservaRepository.buscarPorId(id);

        if (existente == null) {
            throw new IllegalArgumentException(
                    "Reserva não encontrada.");
        }

        if ("CANCELADA".equals(existente.getStatus())
                || "EXPIRADA".equals(existente.getStatus())) {
            throw new IllegalArgumentException(
                    "Não é possível editar uma reserva cancelada ou expirada.");
        }

        Estacionamento estacionamento =
                buscarEstacionamento(novaReserva.getEstacionamentoId());

        if (!estacionamento.isAtivo()) {
            throw new IllegalArgumentException(
                    "O estacionamento está inativo.");
        }

        String vagaAtual = existente.getVagaId();

        if (existente.getEstacionamentoId().equals(
                novaReserva.getEstacionamentoId())
                && existente.getDataHoraInicio().toLocalDate()
                        .equals(novaReserva.getDataHoraInicio().toLocalDate())
                && vagaAtual != null
                && !vagaAtual.isBlank()
                && vagaDisponivel(novaReserva, vagaAtual, id)) {

            novaReserva.setVagaId(vagaAtual);

        } else {
            String vagaLivre = encontrarVagaLivre(
                    novaReserva, estacionamento, id);

            if (vagaLivre == null) {
                throw new IllegalArgumentException(
                        "Não existem vagas disponíveis nesse período.");
            }

            novaReserva.setVagaId(vagaLivre);
        }

        novaReserva.setId(id);
        novaReserva.setStatus(existente.getStatus());
        novaReserva.setDataHoraExpiracao(
                existente.getDataHoraExpiracao());

        return reservaRepository.atualizar(id, novaReserva);
    }

    public Reserva cancelarReserva(String id)
            throws ExecutionException, InterruptedException {

        Reserva reserva = buscarReserva(id);

        if ("CANCELADA".equals(reserva.getStatus())) {
            throw new IllegalArgumentException(
                    "A reserva já está cancelada.");
        }

        if ("EXPIRADA".equals(reserva.getStatus())) {
            throw new IllegalArgumentException(
                    "A reserva já expirou.");
        }

        reserva.setStatus("CANCELADA");

        return reservaRepository.atualizar(id, reserva);
    }

    public void expirarReservasPendentes()
            throws ExecutionException, InterruptedException {

        LocalDateTime agora = LocalDateTime.now();

        List<Reserva> expiradas =
                reservaRepository.listarPendentesExpiradas(agora);

        for (Reserva reserva : expiradas) {
            reservaRepository.atualizarStatus(
                    reserva.getId(), "EXPIRADA");
        }
    }

    private Estacionamento buscarEstacionamento(String estacionamentoId)
            throws ExecutionException, InterruptedException {

        if (estacionamentoId == null || estacionamentoId.isBlank()) {
            throw new IllegalArgumentException(
                    "O estacionamento é obrigatório.");
        }

        for (Estacionamento estacionamento
                : estacionamentoRepository.listarTodos()) {

            if (estacionamentoId.equals(estacionamento.getId())) {
                return estacionamento;
            }
        }

        throw new IllegalArgumentException(
                "Estacionamento não encontrado.");
    }

    private String encontrarVagaLivre(
            Reserva reserva,
            Estacionamento estacionamento,
            String reservaIgnorada)
            throws ExecutionException, InterruptedException {

        for (int numero = 1;
                numero <= estacionamento.getVagasTotais();
                numero++) {

            String vagaId = String.format("ReservaVaga%02d", numero);

            if (vagaDisponivel(reserva, vagaId, reservaIgnorada)) {
                return vagaId;
            }
        }

        return null;
    }

    private boolean vagaDisponivel(
            Reserva novaReserva,
            String vagaId,
            String reservaIgnorada)
            throws ExecutionException, InterruptedException {

        List<Reserva> reservas =
                reservaRepository.listarPorEstacionamentoEDia(
                        novaReserva.getEstacionamentoId(),
                        novaReserva.getDataHoraInicio().toLocalDate());

        for (Reserva existente : reservas) {

            if (reservaIgnorada != null
                    && reservaIgnorada.equals(existente.getId())) {
                continue;
            }

            if ("CANCELADA".equals(existente.getStatus())
                    || "EXPIRADA".equals(existente.getStatus())) {
                continue;
            }

            if (!vagaId.equals(existente.getVagaId())) {
                continue;
            }

            boolean conflito =
                    novaReserva.getDataHoraInicio()
                            .isBefore(existente.getDataHoraFim())
                    && novaReserva.getDataHoraFim()
                            .isAfter(existente.getDataHoraInicio());

            if (conflito) {
                return false;
            }
        }

        return true;
    }

    private void validarDados(Reserva reserva) {

        if (reserva == null) {
            throw new IllegalArgumentException(
                    "Os dados da reserva são obrigatórios.");
        }

        if (reserva.getEstacionamentoId() == null
                || reserva.getEstacionamentoId().isBlank()) {
            throw new IllegalArgumentException(
                    "O estacionamento é obrigatório.");
        }

        if (reserva.getNomeCliente() == null
                || reserva.getNomeCliente().isBlank()) {
            throw new IllegalArgumentException(
                    "O nome do cliente é obrigatório.");
        }

        LocalDateTime inicio = reserva.getDataHoraInicio();
        LocalDateTime fim = reserva.getDataHoraFim();

        if (inicio == null || fim == null) {
            throw new IllegalArgumentException(
                    "As datas e os horários são obrigatórios.");
        }

        if (!fim.isAfter(inicio)) {
            throw new IllegalArgumentException(
                    "O horário final deve ser posterior ao inicial.");
        }

        if (!inicio.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "A reserva deve começar no futuro.");
        }

        if (!inicio.toLocalDate().equals(fim.toLocalDate())) {
            throw new IllegalArgumentException(
                    "A reserva deve começar e terminar no mesmo dia.");
        }
    }

    private void validarId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "O ID da reserva é obrigatório.");
        }
    }
    
    public Map<String, Object> consultarDisponibilidade(
            String estacionamentoId,
            LocalDateTime inicio,
            LocalDateTime fim)
            throws ExecutionException, InterruptedException {

        if (estacionamentoId == null || estacionamentoId.isBlank()) {
            throw new IllegalArgumentException(
                    "O estacionamento é obrigatório.");
        }

        if (inicio == null || fim == null) {
            throw new IllegalArgumentException(
                    "Os horários de início e fim são obrigatórios.");
        }

        if (!fim.isAfter(inicio)) {
            throw new IllegalArgumentException(
                    "O horário final deve ser posterior ao inicial.");
        }

        if (!inicio.toLocalDate().equals(fim.toLocalDate())) {
            throw new IllegalArgumentException(
                    "A consulta deve começar e terminar no mesmo dia.");
        }

        expirarReservasPendentes();

        Estacionamento estacionamento =
                buscarEstacionamento(estacionamentoId);

        List<Reserva> reservas =
                reservaRepository.listarPorEstacionamentoEDia(
                        estacionamentoId, inicio.toLocalDate());

        Set<String> vagasOcupadas = new HashSet<>();

        for (Reserva reserva : reservas) {

            if ("CANCELADA".equals(reserva.getStatus())
                    || "EXPIRADA".equals(reserva.getStatus())) {
                continue;
            }

            boolean conflito =
                    inicio.isBefore(reserva.getDataHoraFim())
                    && fim.isAfter(reserva.getDataHoraInicio());

            if (conflito
                    && reserva.getVagaId() != null
                    && !reserva.getVagaId().isBlank()) {
                vagasOcupadas.add(reserva.getVagaId());
            }
        }

        int total = estacionamento.getVagasTotais();
        int ocupadas = vagasOcupadas.size();
        int disponiveis = Math.max(0, total - ocupadas);

        return Map.of(
                "estacionamentoId", estacionamentoId,
                "dataHoraInicio", inicio.toString(),
                "dataHoraFim", fim.toString(),
                "vagasTotais", total,
                "vagasOcupadas", ocupadas,
                "vagasDisponiveis", disponiveis
        );
    }
}
