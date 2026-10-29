package com.smartpark.controller;

import com.google.cloud.firestore.Firestore;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FirebaseController {

    private final Firestore firestore;

    public FirebaseController(Firestore firestore) {
        this.firestore = firestore;
    }

    @GetMapping("/api/firebase/status")
    public Map<String, String> testarFirebase()
            throws ExecutionException, InterruptedException {

        firestore.collection("teste")
                .document("conexao")
                .set(Map.of("status", "conectado"))
                .get();

        return Map.of(
                "firebase", "conectado",
                "firestore", "funcionando"
        );
    }
}