package br.com.fiap.pluvial.dao;

import br.com.fiap.pluvial.exception.ErroBancoException;
import br.com.fiap.pluvial.exception.RegraNegocioException;
import br.com.fiap.pluvial.model.Leitura;
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
public class LeituraDAO {

    private static final int ORA_PAI_INEXISTENTE = 2291;  // ORA-02291

    private static final String SELECT_BASE =
            "SELECT cd_leitura, cd_sensor, vl_leitura, dt_leitura FROM tb_leitura";

    private final DataSource dataSource;

    public LeituraDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Grava a leitura e atualiza o nível de risco da região do sensor na MESMA transação:
     * ou as duas operações acontecem, ou nenhuma.
     */
    public Leitura registrar(Leitura leitura, String nivelRisco) {
        String sqlSequence = "SELECT sq_tb_leitura.NEXTVAL FROM dual";
        String sqlInsert = "INSERT INTO tb_leitura (cd_leitura, cd_sensor, vl_leitura) VALUES (?, ?, ?)";
        String sqlRisco = "UPDATE tb_regiao SET ds_nivel_risco = ? "
                + "WHERE cd_regiao = (SELECT cd_regiao FROM tb_sensor WHERE cd_sensor = ?)";

        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);
            try {
                long id;
                try (PreparedStatement ps = con.prepareStatement(sqlSequence);
                     ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    id = rs.getLong(1);
                }

                try (PreparedStatement ps = con.prepareStatement(sqlInsert)) {
                    ps.setLong(1, id);
                    ps.setLong(2, leitura.getSensorId());
                    ps.setBigDecimal(3, leitura.getValor());
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(sqlRisco)) {
                    ps.setString(1, nivelRisco);
                    ps.setLong(2, leitura.getSensorId());
                    ps.executeUpdate();
                }

                con.commit();
                return buscar(con, id)
                        .orElseThrow(() -> new ErroBancoException("Leitura não encontrada após o cadastro.", null));
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    public List<Leitura> listar() {
        return consultarLista(SELECT_BASE + " ORDER BY dt_leitura DESC, cd_leitura DESC", null);
    }

    public List<Leitura> listarPorSensor(Long sensorId) {
        return consultarLista(SELECT_BASE + " WHERE cd_sensor = ? ORDER BY dt_leitura DESC, cd_leitura DESC", sensorId);
    }

    public Optional<Leitura> buscarPorId(Long id) {
        try (Connection con = dataSource.getConnection()) {
            return buscar(con, id);
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    public boolean atualizar(Leitura leitura) {
        String sql = "UPDATE tb_leitura SET vl_leitura = ? WHERE cd_leitura = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, leitura.getValor());
            ps.setLong(2, leitura.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    public boolean remover(Long id) {
        String sql = "DELETE FROM tb_leitura WHERE cd_leitura = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    private List<Leitura> consultarLista(String sql, Long filtro) {
        List<Leitura> lista = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (filtro != null) {
                ps.setLong(1, filtro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    private Optional<Leitura> buscar(Connection con, Long id) throws SQLException {
        String sql = SELECT_BASE + " WHERE cd_leitura = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    private Leitura mapear(ResultSet rs) throws SQLException {
        Leitura l = new Leitura();
        l.setId(rs.getLong("cd_leitura"));
        l.setSensorId(rs.getLong("cd_sensor"));
        l.setValor(rs.getBigDecimal("vl_leitura"));
        l.setDataLeitura(rs.getTimestamp("dt_leitura").toLocalDateTime());
        return l;
    }

    private RuntimeException traduzir(SQLException e) {
        return switch (e.getErrorCode()) {
            case ORA_PAI_INEXISTENTE -> new RegraNegocioException("O sensor informado não existe.");
            default -> new ErroBancoException("Erro ao acessar o banco de dados.", e);
        };
    }
}
