
package com.smartpark.model;

public class Estacionamento {

    private String id;
    private String nome;
    private String endereco;
    private int vagasTotais;
    private int vagasDisponiveis;
    private boolean ativo = true;

    public Estacionamento() {
    }

    public Estacionamento(String id, String nome, String endereco,
            int vagasTotais, int vagasDisponiveis, boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.vagasTotais = vagasTotais;
        this.vagasDisponiveis = vagasDisponiveis;
        this.ativo = ativo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public int getVagasTotais() {
        return vagasTotais;
    }

    public void setVagasTotais(int vagasTotais) {
        this.vagasTotais = vagasTotais;
    }

    public int getVagasDisponiveis() {
        return vagasDisponiveis;
    }

    public void setVagasDisponiveis(int vagasDisponiveis) {
        this.vagasDisponiveis = vagasDisponiveis;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
