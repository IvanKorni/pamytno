package net.pamytno.topic.service;

/**
 * Внутреннее событие модуля: файл или каталог хранилища больше не нужен.
 * Удаляется после коммита, чтобы откат транзакции не оставил запись без файла.
 *
 * @param key относительный путь в хранилище
 */
public record StoragePathObsolete(String key) {
}
