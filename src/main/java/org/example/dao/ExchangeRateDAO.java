package org.example.dao;

import org.example.entity.Currency;
import org.example.entity.ExchangeRate;
import org.example.util.ConnectionManager;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeRateDAO {
    private final static ExchangeRateDAO INSTANCE = new ExchangeRateDAO();
    private final CurrencyDAO currencyDAO = CurrencyDAO.getInstance();

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
    private final static String FIND_BY_CURRENCY_CODES_SQL = """
            SELECT er.ID, er.Rate,
                                           c1.ID as BaseCurrencyId, c1.Code as BaseCode, c1.FullName as BaseName, c1.Sign as BaseSign,
                                           c2.ID as TargetCurrencyId, c2.Code as TargetCode, c2.FullName as TargetName, c2.Sign as TargetSign
                                    FROM ExchangeRates er
                                    JOIN Currencies c1 ON er.BaseCurrencyId = c1.ID
                                    JOIN Currencies c2 ON er.TargetCurrencyId = c2.ID
                                    WHERE c1.Code = ? AND c2.Code = ?
            """;
    private final static String UPDATE_SQL = """
            UPDATE ExchangeRates
            SET BaseCurrencyId = ?, TargetCurrencyId = ?, Rate = ?
            WHERE ID = ?
            """;

    public ExchangeRate save(ExchangeRate rate) throws SQLException {
        try (var connection = ConnectionManager.getConnection();
             var statement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, rate.getBaseCurrency().getId());
            statement.setInt(2, rate.getTargetCurrency().getId());
            statement.setBigDecimal(3, rate.getRate());

            statement.executeUpdate();
            ResultSet keys = statement.getGeneratedKeys();
            if (keys.next()) {
                rate.setId(keys.getInt("1"));
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
            statement.setInt(1, rate.getBaseCurrency().getId());
            statement.setInt(2, rate.getTargetCurrency().getId());
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

    public Optional<ExchangeRate> findByCurrencyCodes(String code1, String code2) throws SQLException {
        try (var connection = ConnectionManager.getConnection();
             var statement = connection.prepareStatement(FIND_BY_CURRENCY_CODES_SQL)) {

            statement.setString(1, code1.trim().toUpperCase());
            statement.setString(2, code2.trim().toUpperCase());

            try (var resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    System.out.println("DAO не нашел пару для: [" + code1 + "] и [" + code2 + "]");
                    return Optional.empty();
                }
                return Optional.of(buildExchangeRate(resultSet));
            }
        }
    }

    private ExchangeRate buildExchangeRate(ResultSet resultSet) throws SQLException {
        // Если в ResultSet есть колонки из JOIN (BaseCode, TargetCode и т.д.),
        // лучше брать их сразу, а не мучить CurrencyDAO

        int id = resultSet.getInt(1);
        BigDecimal rate = resultSet.getBigDecimal("Rate");

        // Пытаемся взять данные из JOIN, если они есть (для поиска по кодам)
        // Если их нет (для простого findAll), используем CurrencyDAO
        Currency baseCurrency;
        Currency targetCurrency;

        if (hasColumn(resultSet, "BaseCode")) {
            baseCurrency = new Currency(
                    resultSet.getInt("BaseCurrencyId"),
                    resultSet.getString("BaseCode"),
                    resultSet.getString("BaseName"),
                    resultSet.getString("BaseSign")
            );
            targetCurrency = new Currency(
                    resultSet.getInt("TargetCurrencyId"),
                    resultSet.getString("TargetCode"),
                    resultSet.getString("TargetName"),
                    resultSet.getString("TargetSign")
            );
        } else {
            // Запасной вариант для findAll
            baseCurrency = currencyDAO.findById(resultSet.getInt("BaseCurrencyId")).orElse(null);
            targetCurrency = currencyDAO.findById(resultSet.getInt("TargetCurrencyId")).orElse(null);
        }

        return new ExchangeRate(id, baseCurrency, targetCurrency, rate);
    }

    // Вспомогательный метод, чтобы не падать, если колонок нет
    private boolean hasColumn(ResultSet rs, String columnName) {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }


    public static ExchangeRateDAO getInstance() {
        return INSTANCE;
    }

    private ExchangeRateDAO() {
    }

}
