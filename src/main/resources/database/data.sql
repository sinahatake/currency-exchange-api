INSERT INTO Currencies (Code, FullName, Sign) VALUES
                                                  ('USD', 'United States dollar', '$'),
                                                  ('EUR', 'Euro', '€'),
                                                  ('AUD', 'Australian dollar', 'A$'),
                                                  ('RUB', 'Russian Ruble', '₽');

-- Здесь ID валют будут с 1, 2, 3, 4 соответственно

INSERT INTO ExchangeRates (BaseCurrencyId, TargetCurrencyId, Rate) VALUES
                                                                       (1, 2, 0.99),    -- USD -> EUR
                                                                       (2, 1, 1.01),    -- EUR -> USD
                                                                       (1, 3, 1.45),    -- USD -> AUD
                                                                       (3, 1, 0.69),    -- AUD -> USD
                                                                       (1, 4, 80),      -- USD -> RUB
                                                                       (4, 1, 0.0125);  -- RUB -> USD
