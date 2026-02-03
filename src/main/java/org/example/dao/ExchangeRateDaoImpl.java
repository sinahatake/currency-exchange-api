package org.example.dao;

import org.example.entity.Currency;
import org.example.entity.ExchangeRate;
import org.example.exceptions.AlreadyExistsException;
import org.example.exceptions.DatabaseException;
import org.example.util.ConnectionManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeRateDaoImpl implements ExchangeRateDao {
    private static final ExchangeRateDaoImpl INSTANCE = new ExchangeRateDaoImpl();

    private static final String SAVE_SQL = """
            INSERT INTO ExchangeRates (BaseCurrencyId, TargetCurrencyId, Rate)
            VALUES (?, ?, ?)
            """;

    private static final String FIND_ALL_SQL = """
            SELECT
                er.ID,
                er.Rate,
                c1.ID       AS BaseCurrencyId,
                c1.Code     AS BaseCode,
                c1.FullName AS BaseName,
                c1.Sign     AS BaseSign,
                c2.ID       AS TargetCurrencyId,
                c2.Code     AS TargetCode,
                c2.FullName AS TargetName,
                c2.Sign     AS TargetSign
            FROM ExchangeRates er
            JOIN Currencies c1 ON er.BaseCurrencyId = c1.ID
            JOIN Currencies c2 ON er.TargetCurrencyId = c2.ID
            """;

    private static final String FIND_BY_CURRENCY_CODES_SQL = """
            SELECT
                er.ID,
                er.Rate,
                c1.ID       AS BaseCurrencyId,
                c1.Code     AS BaseCode,
                c1.FullName AS BaseName,
                c1.Sign     AS BaseSign,
                c2.ID       AS TargetCurrencyId,
                c2.Code     AS TargetCode,
                c2.FullName AS TargetName,
                c2.Sign     AS TargetSign
            FROM ExchangeRates er
            JOIN Currencies c1 ON er.BaseCurrencyId = c1.ID
            JOIN Currencies c2 ON er.TargetCurrencyId = c2.ID
            WHERE c1.Code = ? AND c2.Code = ?
            """;

    private static final String UPDATE_SQL = """
            UPDATE ExchangeRates
            SET BaseCurrencyId = ?, TargetCurrencyId = ?, Rate = ?
            WHERE ID = ?
            """;

    private ExchangeRateDaoImpl() {
    }

    public static ExchangeRateDaoImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public ExchangeRate save(ExchangeRate rate) {
        try (var connection = ConnectionManager.getConnection();
             var statement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, rate.getBaseCurrency().getId());
            statement.setInt(2, rate.getTargetCurrency().getId());
            statement.setBigDecimal(3, rate.getRate());

            statement.executeUpdate();
            ResultSet keys = statement.getGeneratedKeys();
            if (keys.next()) {
                rate.setId(keys.getInt(1));
            }
            return rate;
        } catch (SQLException e) {
            if (e.getErrorCode() == 19 || e.getMessage().contains("UNIQUE constraint failed")) {
                throw new AlreadyExistsException(
                        "Exchange rate for " + rate.getBaseCurrency() + "/" + rate.getTargetCurrency() + " already exists"
                );
            }
            throw new DatabaseException("Failed to save exchange rate to database");
        }
    }

    @Override
    public boolean update(ExchangeRate rate) {
        try (var connection = ConnectionManager.getConnection();
             var statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setInt(1, rate.getBaseCurrency().getId());
            statement.setInt(2, rate.getTargetCurrency().getId());
            statement.setBigDecimal(3, rate.getRate());
            statement.setInt(4, rate.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update exchange rate ID: " + rate.getId());
        }
    }

    @Override
    public List<ExchangeRate> findAll() {
        try (var connection = ConnectionManager.getConnection();
             var statement = connection.prepareStatement(FIND_ALL_SQL)) {
            var resultSet = statement.executeQuery();
            List<ExchangeRate> exchangeRates = new ArrayList<>();
            while (resultSet.next()) {
                exchangeRates.add(buildExchangeRate(resultSet));
            }
            return exchangeRates;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch all exchange rates");
        }
    }

    @Override
    public Optional<ExchangeRate> findByCurrencyCodes(String code1, String code2) {
        try (var connection = ConnectionManager.getConnection();
             var statement = connection.prepareStatement(FIND_BY_CURRENCY_CODES_SQL)) {

            statement.setString(1, code1.trim().toUpperCase());
            statement.setString(2, code2.trim().toUpperCase());

            try (var resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(buildExchangeRate(resultSet));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding exchange rate for pair: " + code1 + "/" + code2);
        }
    }

    private ExchangeRate buildExchangeRate(ResultSet rs) throws SQLException {
        Currency base = new Currency(
                rs.getInt("BaseCurrencyId"),
                rs.getString("BaseCode"),
                rs.getString("BaseName"),
                rs.getString("BaseSign")
        );

        Currency target = new Currency(
                rs.getInt("TargetCurrencyId"),
                rs.getString("TargetCode"),
                rs.getString("TargetName"),
                rs.getString("TargetSign")
        );

        return new ExchangeRate(
                rs.getInt("ID"),
                base,
                target,
                rs.getBigDecimal("Rate")
        );
    }
}