package br.com.fiap.pluvial.dao;

import br.com.fiap.pluvial.exception.ErroBancoException;
import br.com.fiap.pluvial.exception.RegraNegocioException;
import br.com.fiap.pluvial.model.Sensor;
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
public class SensorDAO {

    private static final int ORA_PAI_INEXISTENTE = 2291;  // ORA-02291
    private static final int ORA_REGISTRO_FILHO = 2292;   // ORA-02292

    private static final String SELECT_BASE =
            "SELECT cd_sensor, cd_regiao, nm_sensor, ds_tipo, ds_localizacao, vl_limite_alerta, ds_status, dt_instalacao "
                    + "FROM tb_sensor";

    private final DataSource dataSource;

    public SensorDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Sensor inserir(Sensor sensor) {
        String sqlSequence = "SELECT sq_tb_sensor.NEXTVAL FROM dual";
        String sqlInsert = "INSERT INTO tb_sensor (cd_sensor, cd_regiao, nm_sensor, ds_tipo, ds_localizacao, vl_limite_alerta, ds_status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = dataSource.getConnection()) {
            long id;
            try (PreparedStatement ps = con.prepareStatement(sqlSequence);
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                id = rs.getLong(1);
            }

            try (PreparedStatement ps = con.prepareStatement(sqlInsert)) {
                ps.setLong(1, id);
                ps.setLong(2, sensor.getRegiaoId());
                ps.setString(3, sensor.getNome());
                ps.setString(4, sensor.getTipo());
                ps.setString(5, sensor.getLocalizacao());
                ps.setBigDecimal(6, sensor.getLimiteAlerta());
                ps.setString(7, sensor.getStatus());
                ps.executeUpdate();
            }

            return buscar(con, id)
                    .orElseThrow(() -> new ErroBancoException("Sensor não encontrado após o cadastro.", null));
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    public List<Sensor> listar() {
        return consultarLista(SELECT_BASE + " ORDER BY cd_sensor", null);
    }

    public List<Sensor> listarPorRegiao(Long regiaoId) {
        return consultarLista(SELECT_BASE + " WHERE cd_regiao = ? ORDER BY cd_sensor", regiaoId);
    }

    public Optional<Sensor> buscarPorId(Long id) {
        try (Connection con = dataSource.getConnection()) {
            return buscar(con, id);
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    public boolean atualizar(Sensor sensor) {
        String sql = "UPDATE tb_sensor SET cd_regiao = ?, nm_sensor = ?, ds_tipo = ?, ds_localizacao = ?, "
                + "vl_limite_alerta = ?, ds_status = ? WHERE cd_sensor = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, sensor.getRegiaoId());
            ps.setString(2, sensor.getNome());
            ps.setString(3, sensor.getTipo());
            ps.setString(4, sensor.getLocalizacao());
            ps.setBigDecimal(5, sensor.getLimiteAlerta());
            ps.setString(6, sensor.getStatus());
            ps.setLong(7, sensor.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    public boolean remover(Long id) {
        String sql = "DELETE FROM tb_sensor WHERE cd_sensor = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    private List<Sensor> consultarLista(String sql, Long filtro) {
        List<Sensor> lista = new ArrayList<>();

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

    private Optional<Sensor> buscar(Connection con, Long id) throws SQLException {
        String sql = SELECT_BASE + " WHERE cd_sensor = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    private Sensor mapear(ResultSet rs) throws SQLException {
        Sensor s = new Sensor();
        s.setId(rs.getLong("cd_sensor"));
        s.setRegiaoId(rs.getLong("cd_regiao"));
        s.setNome(rs.getString("nm_sensor"));
        s.setTipo(rs.getString("ds_tipo"));
        s.setLocalizacao(rs.getString("ds_localizacao"));
        s.setLimiteAlerta(rs.getBigDecimal("vl_limite_alerta"));
        s.setStatus(rs.getString("ds_status"));
        s.setDataInstalacao(rs.getTimestamp("dt_instalacao").toLocalDateTime());
        return s;
    }

    private RuntimeException traduzir(SQLException e) {
        return switch (e.getErrorCode()) {
            case ORA_PAI_INEXISTENTE -> new RegraNegocioException("A região informada não existe.");
            case ORA_REGISTRO_FILHO -> new RegraNegocioException(
                    "Não é possível remover o sensor: ele possui leituras registradas.");
            default -> new ErroBancoException("Erro ao acessar o banco de dados.", e);
        };
    }
}
