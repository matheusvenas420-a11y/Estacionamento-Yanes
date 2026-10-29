
package com.smartpark;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TesteConexao {

    public static void main(String[] args) {
        String url = "jdbc:mariadb://localhost:3306/smartpark";
        String usuario = "root";
        String senha = "";

        try (Connection conexao =
                     DriverManager.getConnection(url, usuario, senha)) {

            System.out.println("Conexão com MariaDB realizada com sucesso!");
            System.out.println("Banco: " +
                    conexao.getCatalog());

        } catch (SQLException e) {
            System.out.println("Erro ao conectar ao MariaDB:");
            System.out.println(e.getMessage());
        }
    }
}
