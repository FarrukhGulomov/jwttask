package uz.pdp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private String fromCardNumber;
    private String toCardNumber;
    private BigDecimal amount;
    private BigDecimal commission;
    private LocalDateTime timestamp;
}
