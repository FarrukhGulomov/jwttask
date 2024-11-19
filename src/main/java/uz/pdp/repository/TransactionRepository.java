package uz.pdp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.entity.Card;
import uz.pdp.entity.Transaction;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByFromCardOrToCard(Card fromCard, Card toCard);
    List<Transaction> findAllByFromCardInOrToCardIn(List<Card> fromCards, List<Card> toCards);
}
