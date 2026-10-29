
package com.smartpark.repository;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.smartpark.model.Estacionamento;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.springframework.stereotype.Repository;

@Repository
public class EstacionamentoFirebaseRepository {

    private final Firestore firestore;

    public EstacionamentoFirebaseRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public Estacionamento salvar(Estacionamento estacionamento)
            throws ExecutionException, InterruptedException {

        DocumentReference documento;

        if (estacionamento.getId() == null
                || estacionamento.getId().isBlank()) {
            documento = firestore.collection("estacionamentos").document();
        } else {
            documento = firestore.collection("estacionamentos")
                    .document(estacionamento.getId());
        }

        documento.set(gerarDados(estacionamento)).get();
        estacionamento.setId(documento.getId());

        return estacionamento;
    }

    public Estacionamento atualizar(String id,
            Estacionamento estacionamento)
            throws ExecutionException, InterruptedException {

        DocumentReference documento = firestore
                .collection("estacionamentos")
                .document(id);

        DocumentSnapshot existente = documento.get().get();

        if (!existente.exists()) {
            return null;
        }

        documento.update(gerarDados(estacionamento)).get();
        estacionamento.setId(id);

        return estacionamento;
    }

    public boolean excluir(String id)
            throws ExecutionException, InterruptedException {

        DocumentReference documento = firestore
                .collection("estacionamentos")
                .document(id);

        DocumentSnapshot existente = documento.get().get();

        if (!existente.exists()) {
            return false;
        }

        documento.delete().get();
        return true;
    }

    public List<Estacionamento> listarTodos()
            throws ExecutionException, InterruptedException {

        List<QueryDocumentSnapshot> documentos = firestore
                .collection("estacionamentos")
                .get()
                .get()
                .getDocuments();

        List<Estacionamento> estacionamentos = new ArrayList<>();

        for (DocumentSnapshot documento : documentos) {
            Estacionamento estacionamento = new Estacionamento();

            estacionamento.setId(documento.getId());
            estacionamento.setNome(documento.getString("nome"));
            estacionamento.setEndereco(documento.getString("endereco"));

            Long totais = documento.getLong("vagasTotais");
            Long disponiveis = documento.getLong("vagasDisponiveis");

            estacionamento.setVagasTotais(
                    totais == null ? 0 : totais.intValue());

            estacionamento.setVagasDisponiveis(
                    disponiveis == null ? 0 : disponiveis.intValue());

            Boolean ativo = documento.getBoolean("ativo");
            estacionamento.setAtivo(ativo == null || ativo);

            estacionamentos.add(estacionamento);
        }

        return estacionamentos;
    }

    private Map<String, Object> gerarDados(
            Estacionamento estacionamento) {

        Map<String, Object> dados = new HashMap<>();

        dados.put("nome", estacionamento.getNome());
        dados.put("endereco", estacionamento.getEndereco());
        dados.put("vagasTotais", estacionamento.getVagasTotais());
        dados.put("vagasDisponiveis",
                estacionamento.getVagasDisponiveis());
        dados.put("ativo", estacionamento.isAtivo());

        return dados;
    }
}
