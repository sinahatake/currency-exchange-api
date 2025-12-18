package org.example.jdbc.dao;

import org.example.jdbc.entity.ExchangeRate;
import org.example.jdbc.util.ConnectionManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeRateDAO {
    private final static ExchangeRateDAO INSTANCE = new ExchangeRateDAO();

    private final static String SAVE_SQL = """
            INSERT INTO ExchangeRates
            (BaseCurrencyId, TargetCurrencyId, Rate) VALUES (?, ?, ?)
            """;
    private final static String DELETE_SQL = """
            DELETE FROM ExchangeRates
            WHERE ID = ?
            """;
    private final static String FIND_ALL_SQL = """
            SELECT ID, BaseCurrencyId, TargetCurrencyId, Rate
            FROM ExchangeRates
            """;
    private final static String FIND_BY_CURRENCY_IDS_SQL = """
            SELECT ID, BaseCurrencyId, TargetCurrencyId, Rate
            FROM ExchangeRates
            WHERE BaseCurrencyId = ?
            AND TargetCurrencyId = ?
            """;
    private final static String UPDATE_SQL = """
            UPDATE ExchangeRates
            SET BaseCurrencyId = ?, TargetCurrencyId = ?, Rate = ?
            WHERE ID = ?
            """;

    public ExchangeRate save(ExchangeRate rate) throws SQLException {
        try (var connection = ConnectionManager.getConnection();
             var statement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, rate.getBaseCurrencyId());
            statement.setInt(2, rate.getTargetCurrencyId());
            statement.setBigDecimal(3, rate.getRate());

            statement.executeUpdate();
            ResultSet keys = statement.getGeneratedKeys();
            if (keys.next()) {
                rate.setId(keys.getInt("ID"));
            }
            return rate;

        }

    }

    public boolean delete(int id) throws SQLException {
        try (var connection = ConnectionManager.getConnection();
             var statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean update(ExchangeRate rate) throws SQLException {
        try (var connection = ConnectionManager.getConnection();
             var statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setInt(1, rate.getBaseCurrencyId());
            statement.setInt(2, rate.getTargetCurrencyId());
            statement.setBigDecimal(3, rate.getRate());
            statement.setInt(4, rate.getId());
            return statement.executeUpdate() > 0;
        }
    }

    public List<ExchangeRate> findAll() throws SQLException {
        try (var connection = ConnectionManager.getConnection();
             var statement = connection.prepareStatement(FIND_ALL_SQL)) {
            var resultSet = statement.executeQuery();
            List<ExchangeRate> list = new ArrayList<>();
            while (resultSet.next()) {
                list.add(buildExchangeRate(resultSet));
            }
            return list;
        }
    }

    public Optional<ExchangeRate> findByCurrencyIds(int baseCurrencyId, int targetCurrencyId) throws SQLException {
        try (var connection = ConnectionManager.getConnection();
             var statement = connection.prepareStatement(FIND_BY_CURRENCY_IDS_SQL)) {

            statement.setInt(1, baseCurrencyId);
            statement.setInt(2, targetCurrencyId);

            try (var resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(buildExchangeRate(resultSet));
            }

        }
    }

    private ExchangeRate buildExchangeRate(ResultSet resultSet) throws SQLException {
        ExchangeRate rate = new ExchangeRate();
        rate.setId(resultSet.getInt("ID"));
        rate.setBaseCurrencyId(resultSet.getInt("BaseCurrencyId"));
        rate.setTargetCurrencyId(resultSet.getInt("TargetCurrencyId"));
        rate.setRate(resultSet.getBigDecimal("Rate"));
        return rate;
    }


    public static ExchangeRateDAO getInstance() {
        return INSTANCE;
    }

    private ExchangeRateDAO() {
    }

}
