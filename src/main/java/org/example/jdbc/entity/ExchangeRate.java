package org.example.jdbc.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeRate {
    private int id;
    private int baseCurrencyId;
    private int targetCurrencyId;
    private BigDecimal rate;
}
