package uz.pdp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uz.pdp.dto.CardResponse;
import uz.pdp.entity.Card;
import uz.pdp.entity.User;
import uz.pdp.exception.InvalidCardException;
import uz.pdp.repository.CardRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {
    private final CardRepository cardRepository;

    public CardResponse createCard(String cardNumber) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Creating card for user: {}", currentUser.getEmail());

        if (cardRepository.existsByCardNumber(cardNumber)) {
            throw new InvalidCardException("Card number already exists");
        }

        Card card = Card.builder()
                .cardNumber(cardNumber)
                .balance(BigDecimal.ZERO)
                .user(currentUser)
                .build();

        card = cardRepository.save(card);
        log.info("Card created with ID: {}", card.getId());

        return toCardResponse(card);
    }

    public List<CardResponse> getCurrentUserCards() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Getting cards for user: {}", currentUser.getEmail());
        List<Card> cards = cardRepository.findAllByUserId(currentUser.getId());
        return cards.stream()
                .map(this::toCardResponse)
                .collect(Collectors.toList());
    }

    public CardResponse addBalance(String cardNumber, BigDecimal amount) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Adding balance for card: {} by user: {}", cardNumber, currentUser.getEmail());

        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new InvalidCardException("Card not found"));

        log.info("Found card with ID: {} owned by user: {}", card.getId(), card.getUser().getEmail());

        if (!card.getUser().getId().equals(currentUser.getId())) {
            log.warn("User {} attempted to add balance to card owned by {}", currentUser.getEmail(), card.getUser().getEmail());
            throw new InvalidCardException("You can only add balance to your own cards");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid amount attempted: {}", amount);
            throw new InvalidCardException("Amount must be positive");
        }

        card.setBalance(card.getBalance().add(amount));
        card = cardRepository.save(card);
        log.info("Balance updated for card: {}. New balance: {}", cardNumber, card.getBalance());

        return toCardResponse(card);
    }

    private CardResponse toCardResponse(Card card) {
        return CardResponse.builder()
                .id(card.getId())
                .cardNumber(card.getCardNumber())
                .balance(card.getBalance())
                .build();
    }
}
