package uz.pdp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.dto.CardResponse;
import uz.pdp.service.CardService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@CrossOrigin
@Slf4j
public class CardController {
    private final CardService cardService;

    @PostMapping
    public ResponseEntity<CardResponse> createCard(@RequestParam String cardNumber) {
        log.info("Creating card with number: {}", cardNumber);
        return ResponseEntity.ok(cardService.createCard(cardNumber));
    }

    @GetMapping
    public ResponseEntity<List<CardResponse>> getCurrentUserCards() {
        log.info("Getting current user cards");
        return ResponseEntity.ok(cardService.getCurrentUserCards());
    }

    @PutMapping("/{cardNumber}/balance")
    public ResponseEntity<CardResponse> addBalance(
            @PathVariable String cardNumber,
            @RequestParam BigDecimal amount) {
        log.info("Adding balance {} to card {}", amount, cardNumber);
        return ResponseEntity.ok(cardService.addBalance(cardNumber, amount));
    }
}
