INSERT INTO Currencies (Code, FullName, Sign)
VALUES ('USD', 'United States dollar', '$'),
       ('EUR', 'Euro', '€'),
       ('AUD', 'Australian dollar', 'A$'),
       ('RUB', 'Russian Ruble', '₽');


INSERT INTO ExchangeRates (BaseCurrencyId, TargetCurrencyId, Rate)
VALUES (1, 2, 0.99),
       (2, 1, 1.01),
       (1, 3, 1.45),
       (3, 1, 0.69),
       (1, 4, 80),
       (4, 1, 0.01);
