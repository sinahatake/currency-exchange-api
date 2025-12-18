CREATE TABLE IF NOT EXISTS Currencies (
                                          ID INTEGER PRIMARY KEY AUTOINCREMENT,
                                          Code VARCHAR(3) UNIQUE NOT NULL,
                                          FullName VARCHAR(100) NOT NULL,
                                          Sign VARCHAR(5)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_currency_code ON Currencies(Code);

CREATE TABLE IF NOT EXISTS ExchangeRates (
                                             ID INTEGER PRIMARY KEY AUTOINCREMENT,
                                             BaseCurrencyId INTEGER NOT NULL,
                                             TargetCurrencyId INTEGER NOT NULL,
                                             Rate DECIMAL(10,6) NOT NULL,
                                             FOREIGN KEY (BaseCurrencyId) REFERENCES Currencies(ID),
                                             FOREIGN KEY (TargetCurrencyId) REFERENCES Currencies(ID),
                                             UNIQUE (BaseCurrencyId, TargetCurrencyId)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_exchange_pair ON ExchangeRates(BaseCurrencyId, TargetCurrencyId);
