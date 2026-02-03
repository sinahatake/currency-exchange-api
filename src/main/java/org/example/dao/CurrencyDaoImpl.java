package org.example.dao;

import org.example.entity.Currency;
import org.example.exceptions.AlreadyExistsException;
import org.example.exceptions.DatabaseException;
import org.example.util.ConnectionManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CurrencyDaoImpl implements CurrencyDao {
    private static final CurrencyDaoImpl INSTANCE = new CurrencyDaoImpl();

    private static final String SAVE_SQL = "INSERT INTO Currencies (Code, FullName, Sign) VALUES (?, ?, ?)";
    private static final String FIND_ALL_SQL = "SELECT ID, Code, FullName, Sign FROM Currencies";
    private static final String FIND_BY_CODE_SQL = "SELECT ID, Code, FullName, Sign FROM Currencies WHERE Code = ?";

    private CurrencyDaoImpl() {
    }

    public static CurrencyDaoImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public Currency save(Currency currency) {
        try (var connection = ConnectionManager.getConnection();
             var statement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, currency.getCode());
            statement.setString(2, currency.getFullName());
            statement.setString(3, currency.getSign());

            statement.executeUpdate();
            ResultSet keys = statement.getGeneratedKeys();
            if (keys.next()) {
                currency.setId(keys.getInt(1));
            }
            return currency;
        } catch (SQLException e) {
            if (e.getErrorCode() == 19 || e.getMessage().contains("UNIQUE")) {
                throw new AlreadyExistsException("Currency with code " + currency.getCode() + " already exists");
            }
            throw new DatabaseException("Failed to save currency: " + currency.getCode());
        }
    }

    @Override
    public List<Currency> findAll() {
        try (var connection = ConnectionManager.getConnection();
             var statement = connection.prepareStatement(FIND_ALL_SQL)) {
            var resultSet = statement.executeQuery();
            List<Currency> currencies = new ArrayList<>();
            while (resultSet.next()) {
                currencies.add(buildCurrency(resultSet));
            }
            return currencies;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve currencies from database");
        }
    }

    @Override
    public Optional<Currency> findByCode(String code) {
        try (var connection = ConnectionManager.getConnection();
             var statement = connection.prepareStatement(FIND_BY_CODE_SQL)) {
            statement.setString(1, code);
            try (var resultSet = statement.executeQuery()) {
                if (!resultSet.next()) return Optional.empty();
                return Optional.of(buildCurrency(resultSet));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error searching for currency by code: " + code);
        }
    }

    private Currency buildCurrency(ResultSet resultSet) throws SQLException {
        Currency currency = new Currency();
        currency.setId(resultSet.getInt(1));
        currency.setCode(resultSet.getString("Code"));
        currency.setFullName(resultSet.getString("FullName"));
        currency.setSign(resultSet.getString("Sign"));
        return currency;
    }
}