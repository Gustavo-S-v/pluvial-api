package br.com.fiap.pluvial.dao;

import br.com.fiap.pluvial.exception.ErroBancoException;
import br.com.fiap.pluvial.exception.RegraNegocioException;
import br.com.fiap.pluvial.model.Regiao;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class RegiaoDAO {

    private static final int ORA_UNIQUE_VIOLADA = 1;      // ORA-00001
    private static final int ORA_REGISTRO_FILHO = 2292;   // ORA-02292

    private static final String SELECT_BASE =
            "SELECT cd_regiao, nm_regiao, nm_cidade, ds_nivel_risco, dt_cadastro FROM tb_regiao";

    private final DataSource dataSource;

    public RegiaoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Regiao inserir(Regiao regiao) {
        String sqlSequence = "SELECT sq_tb_regiao.NEXTVAL FROM dual";
        String sqlInsert = "INSERT INTO tb_regiao (cd_regiao, nm_regiao, nm_cidade) VALUES (?, ?, ?)";

        try (Connection con = dataSource.getConnection()) {
            long id;
            try (PreparedStatement ps = con.prepareStatement(sqlSequence);
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                id = rs.getLong(1);
            }

            try (PreparedStatement ps = con.prepareStatement(sqlInsert)) {
                ps.setLong(1, id);
                ps.setString(2, regiao.getNome());
                ps.setString(3, regiao.getCidade());
                ps.executeUpdate();
            }

            // nível de risco e data de cadastro vêm dos valores padrão do banco
            return buscar(con, id)
                    .orElseThrow(() -> new ErroBancoException("Região não encontrada após o cadastro.", null));
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    public List<Regiao> listar() {
        String sql = SELECT_BASE + " ORDER BY cd_regiao";
        List<Regiao> lista = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    public Optional<Regiao> buscarPorId(Long id) {
        try (Connection con = dataSource.getConnection()) {
            return buscar(con, id);
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    public boolean atualizar(Regiao regiao) {
        String sql = "UPDATE tb_regiao SET nm_regiao = ?, nm_cidade = ? WHERE cd_regiao = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, regiao.getNome());
            ps.setString(2, regiao.getCidade());
            ps.setLong(3, regiao.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    public boolean remover(Long id) {
        String sql = "DELETE FROM tb_regiao WHERE cd_regiao = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    private Optional<Regiao> buscar(Connection con, Long id) throws SQLException {
        String sql = SELECT_BASE + " WHERE cd_regiao = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    private Regiao mapear(ResultSet rs) throws SQLException {
        Regiao r = new Regiao();
        r.setId(rs.getLong("cd_regiao"));
        r.setNome(rs.getString("nm_regiao"));
        r.setCidade(rs.getString("nm_cidade"));
        r.setNivelRisco(rs.getString("ds_nivel_risco"));
        r.setDataCadastro(rs.getTimestamp("dt_cadastro").toLocalDateTime());
        return r;
    }

    private RuntimeException traduzir(SQLException e) {
        return switch (e.getErrorCode()) {
            case ORA_UNIQUE_VIOLADA -> new RegraNegocioException("Já existe uma região com este nome nesta cidade.");
            case ORA_REGISTRO_FILHO -> new RegraNegocioException(
                    "Não é possível remover a região: ela possui sensores cadastrados.");
            default -> new ErroBancoException("Erro ao acessar o banco de dados.", e);
        };
    }
}
