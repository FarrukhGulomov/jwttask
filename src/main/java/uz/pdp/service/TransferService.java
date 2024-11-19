package uz.pdp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.dto.TransactionResponse;
import uz.pdp.dto.TransferRequest;
import uz.pdp.entity.Card;
import uz.pdp.entity.Transaction;
import uz.pdp.entity.User;
import uz.pdp.exception.InsufficientFundsException;
import uz.pdp.exception.InvalidCardException;
import uz.pdp.repository.CardRepository;
import uz.pdp.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransferService {
    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.01"); // 1% commission

    @Transactional
    public TransactionResponse transfer(TransferRequest request) {
        Card fromCard = cardRepository.findByCardNumber(request.getFromCardNumber())
                .orElseThrow(() -> new InvalidCardException("From card not found"));

        Card toCard = cardRepository.findByCardNumber(request.getToCardNumber())
                .orElseThrow(() -> new InvalidCardException("To card not found"));

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!fromCard.getUser().getId().equals(currentUser.getId())) {
            throw new InvalidCardException("You can only transfer money from your own cards");
        }

        BigDecimal amount = request.getAmount();
        BigDecimal commission = amount.multiply(COMMISSION_RATE);
        BigDecimal totalDeduction = amount.add(commission);

        if (fromCard.getBalance().compareTo(totalDeduction) < 0) {
            throw new InsufficientFundsException("Insufficient funds for transfer");
        }

        // Update balances
        fromCard.setBalance(fromCard.getBalance().subtract(totalDeduction));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        // Create and save transaction
        Transaction transaction = Transaction.builder()
                .fromCard(fromCard)
                .toCard(toCard)
                .amount(amount)
                .commission(commission)
                .timestamp(LocalDateTime.now())
                .build();

        transaction = transactionRepository.save(transaction);

        return TransactionResponse.builder()
                .id(transaction.getId())
                .fromCardNumber(fromCard.getCardNumber())
                .toCardNumber(toCard.getCardNumber())
                .amount(amount)
                .commission(commission)
                .timestamp(transaction.getTimestamp())
                .build();
    }

    public List<TransactionResponse> getTransactionHistory() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Card> userCards = cardRepository.findAllByUserId(currentUser.getId());
        List<Transaction> transactions = transactionRepository.findAllByFromCardInOrToCardIn(userCards, userCards);

        return transactions.stream()
                .map(transaction -> TransactionResponse.builder()
                        .id(transaction.getId())
                        .fromCardNumber(transaction.getFromCard().getCardNumber())
                        .toCardNumber(transaction.getToCard().getCardNumber())
                        .amount(transaction.getAmount())
                        .commission(transaction.getCommission())
                        .timestamp(transaction.getTimestamp())
                        .build())
                .collect(Collectors.toList());
    }
}
