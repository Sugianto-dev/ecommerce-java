package com.fastcampus.ecommerce.model;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(SnakeCaseStrategy.class)
public class PaymentResponse {
    private String xenditInvoiceId;
    private String xenditExternalId;
    private BigDecimal amount;
    private String xenditInvoiceStatus;
    private String xenditPaymentUrl;
}
