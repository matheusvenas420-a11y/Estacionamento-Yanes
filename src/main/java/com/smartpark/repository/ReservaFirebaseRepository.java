
package com.smartpark.repository;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.smartpark.model.Reserva;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.springframework.stereotype.Repository;

@Repository
public class ReservaFirebaseRepository {

    private final Firestore firestore;

    public ReservaFirebaseRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    private DocumentReference referenciaReserva(
            String estacionamentoId, String data, String reservaId) {

        return firestore.collection("estacionamentos")
                .document(estacionamentoId)
                .collection("reservasPorDia")
                .document(data)
                .collection("reservas")
                .document(reservaId);
    }

    public Reserva salvar(Reserva reserva)
            throws ExecutionException, InterruptedException {

        String data = reserva.getDataHoraInicio()
                .toLocalDate().toString();

        DocumentReference documento = firestore
                .collection("estacionamentos")
                .document(reserva.getEstacionamentoId())
                .collection("reservasPorDia")
                .document(data)
                .collection("reservas")
                .document();

        documento.set(gerarDados(reserva)).get();
        reserva.setId(documento.getId());

        return reserva;
    }

    public Reserva buscarPorId(String id)
            throws ExecutionException, InterruptedException {

        for (Reserva reserva : listarTodas()) {
            if (id.equals(reserva.getId())) {
                return reserva;
            }
        }

        return null;
    }

    public List<Reserva> listarTodas()
            throws ExecutionException, InterruptedException {

        List<Reserva> reservas = new ArrayList<>();

        List<QueryDocumentSnapshot> documentos = firestore
                .collectionGroup("reservas")
                .get()
                .get()
                .getDocuments();

        for (DocumentSnapshot documento : documentos) {
            String[] partes = documento.getReference()
                    .getPath().split("/");

            if (partes.length >= 6
                    && "estacionamentos".equals(partes[0])
                    && "reservasPorDia".equals(partes[2])
                    && "reservas".equals(partes[4])) {

                String estacionamentoId = partes[1];

                reservas.add(converterReserva(
                        documento, estacionamentoId));
            }
        }

        return reservas;
    }

    public Reserva atualizar(String id, Reserva reserva)
            throws ExecutionException, InterruptedException {

        Reserva existente = buscarPorId(id);

        if (existente == null) {
            return null;
        }

        String dataAntiga = existente.getDataHoraInicio()
                .toLocalDate().toString();

        String dataNova = reserva.getDataHoraInicio()
                .toLocalDate().toString();

        DocumentReference documentoAntigo = referenciaReserva(
                existente.getEstacionamentoId(), dataAntiga, id);

        if (existente.getEstacionamentoId().equals(
                reserva.getEstacionamentoId())
                && dataAntiga.equals(dataNova)) {

            documentoAntigo.set(gerarDados(reserva)).get();

        } else {
            DocumentReference documentoNovo = referenciaReserva(
                    reserva.getEstacionamentoId(), dataNova, id);

            documentoNovo.set(gerarDados(reserva)).get();
            documentoAntigo.delete().get();
        }

        reserva.setId(id);
        return reserva;
    }

    public boolean excluir(String id)
            throws ExecutionException, InterruptedException {

        Reserva reserva = buscarPorId(id);

        if (reserva == null) {
            return false;
        }

        String data = reserva.getDataHoraInicio()
                .toLocalDate().toString();

        referenciaReserva(
                reserva.getEstacionamentoId(), data, id)
                .delete().get();

        return true;
    }
   
    public List<Reserva> listarPorEstacionamentoEDia(
            String estacionamentoId, LocalDate data)
            throws ExecutionException, InterruptedException {

        List<Reserva> todas = listarTodas();
        List<Reserva> reservasDoDia = new ArrayList<>();

        System.out.println("=== DIAGNÓSTICO DE RESERVAS ===");
        System.out.println("Estacionamento recebido: " + estacionamentoId);
        System.out.println("Data recebida: " + data);
        System.out.println("Total de reservas: " + todas.size());

        for (Reserva reserva : todas) {
            System.out.println(
                    "ID: " + reserva.getId()
                    + " | Estacionamento: " + reserva.getEstacionamentoId()
                    + " | Início: " + reserva.getDataHoraInicio());

            if (estacionamentoId.equals(reserva.getEstacionamentoId())
                    && reserva.getDataHoraInicio() != null
                    && reserva.getDataHoraInicio().toLocalDate().equals(data)) {
                reservasDoDia.add(reserva);
            }
        }

        System.out.println("Reservas encontradas: " + reservasDoDia.size());
        System.out.println("================================");

        return reservasDoDia;
    }

    public Reserva atualizarStatus(String id, String status)
            throws ExecutionException, InterruptedException {

        Reserva reserva = buscarPorId(id);

        if (reserva == null) {
            return null;
        }

        String data = reserva.getDataHoraInicio()
                .toLocalDate().toString();

        DocumentReference documento = referenciaReserva(
                reserva.getEstacionamentoId(), data, id);

        documento.update("status", status).get();
        reserva.setStatus(status);

        return reserva;
    }

    public List<Reserva> listarPendentesExpiradas(
            LocalDateTime agora)
            throws ExecutionException, InterruptedException {

        List<Reserva> expiradas = new ArrayList<>();

        for (Reserva reserva : listarTodas()) {
            if ("PENDENTE".equals(reserva.getStatus())
                    && reserva.getDataHoraExpiracao() != null
                    && !reserva.getDataHoraExpiracao().isAfter(agora)) {

                expiradas.add(reserva);
            }
        }

        return expiradas;
    }

    private Map<String, Object> gerarDados(Reserva reserva) {
        Map<String, Object> dados = new HashMap<>();

        dados.put("nomeCliente", reserva.getNomeCliente());

        dados.put("dataHoraInicio",
                reserva.getDataHoraInicio().toString());

        dados.put("dataHoraFim",
                reserva.getDataHoraFim().toString());

        dados.put("dataHoraExpiracao",
                reserva.getDataHoraExpiracao() == null
                        ? null
                        : reserva.getDataHoraExpiracao().toString());

        dados.put("vagaId", reserva.getVagaId());
        dados.put("status", reserva.getStatus());

        return dados;
    }

    private Reserva converterReserva(
            DocumentSnapshot documento, String estacionamentoId) {

        Reserva reserva = new Reserva();

        reserva.setId(documento.getId());
        reserva.setEstacionamentoId(estacionamentoId);
        reserva.setNomeCliente(
                documento.getString("nomeCliente"));

        String inicio = documento.getString("dataHoraInicio");
        String fim = documento.getString("dataHoraFim");
        String expiracao = documento.getString("dataHoraExpiracao");

        if (inicio != null) {
            reserva.setDataHoraInicio(
                    LocalDateTime.parse(inicio));
        }

        if (fim != null) {
            reserva.setDataHoraFim(
                    LocalDateTime.parse(fim));
        }

        if (expiracao != null) {
            reserva.setDataHoraExpiracao(
                    LocalDateTime.parse(expiracao));
        }

        reserva.setVagaId(documento.getString("vagaId"));

        String status = documento.getString("status");
        reserva.setStatus(
                status == null ? "PENDENTE" : status);

        return reserva;
    }
}
