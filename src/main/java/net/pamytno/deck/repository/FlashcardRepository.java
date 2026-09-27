package net.pamytno.deck.repository;

import net.pamytno.deck.domain.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Хранилище карточек.
 */
public interface FlashcardRepository extends JpaRepository<Flashcard, UUID> {
}
