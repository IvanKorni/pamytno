package net.pamytno.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Transient;
import lombok.Getter;
import org.springframework.data.domain.Persistable;

import java.util.Objects;
import java.util.UUID;

/**
 * База для JPA-сущностей: идентификатор UUID назначается в момент создания объекта,
 * а не базой данных, поэтому сущность можно сразу сослаться в событии.
 * Реализует {@link Persistable}, чтобы Spring Data делал {@code persist}, а не лишний {@code merge}.
 */
@Getter
@MappedSuperclass
public abstract class BaseEntity implements Persistable<UUID> {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Transient
    private boolean fresh;

    /**
     * Конструктор для JPA.
     */
    protected BaseEntity() {
    }

    /**
     * Создаёт новую сущность с новым идентификатором.
     *
     * @param id идентификатор новой сущности
     */
    protected BaseEntity(UUID id) {
        this.id = id;
        this.fresh = true;
    }

    /**
     * Сообщает Spring Data, что сущность ещё не сохранялась.
     *
     * @return {@code true}, если сущность создана и ещё не сохранена
     */
    @Override
    public boolean isNew() {
        return fresh;
    }

    /**
     * После сохранения или загрузки сущность перестаёт быть новой.
     */
    @PostLoad
    @PostPersist
    void markPersisted() {
        this.fresh = false;
    }

    /**
     * Сущности равны, если совпадают класс и идентификатор.
     *
     * @param other другой объект
     * @return {@code true}, если это та же сущность
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other != null && getClass() == other.getClass() && Objects.equals(id, ((BaseEntity) other).id);
    }

    /**
     * Хеш-код по классу — стабилен до и после сохранения.
     *
     * @return хеш-код
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
