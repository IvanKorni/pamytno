import { describe, expect, it } from 'vitest'
import { isShortText, parseInline, parseRichText, plainText, splitFirstLine } from './richText'

describe('разметка текста карточки', () => {
  it('делит текст на абзацы по пустой строке и сохраняет переносы внутри абзаца', () => {
    // when
    const blocks = parseRichText('Первая строка\nвторая строка\n\nНовый абзац')

    // then
    expect(blocks).toEqual([
      { kind: 'paragraph', text: 'Первая строка\nвторая строка' },
      { kind: 'paragraph', text: 'Новый абзац' },
    ])
  })

  it('собирает варианты ответа задачи, даже если между ними пустые строки', () => {
    // when
    const blocks = parseRichText('Какой подход выбрать?\nA. CQRS\n\nB) Шардирование\nC. Очередь')

    // then
    expect(blocks).toEqual([
      { kind: 'paragraph', text: 'Какой подход выбрать?' },
      {
        kind: 'options',
        items: [
          { marker: 'A', text: 'CQRS' },
          { marker: 'B', text: 'Шардирование' },
          { marker: 'C', text: 'Очередь' },
        ],
      },
    ])
  })

  it('одну строку, похожую на вариант, оставляет абзацем', () => {
    // when
    const blocks = parseRichText('А. С. Пушкин писал о памяти')

    // then
    expect(blocks).toEqual([{ kind: 'paragraph', text: 'А. С. Пушкин писал о памяти' }])
  })

  it('разбирает маркированный и нумерованный списки', () => {
    // when
    const blocks = parseRichText('- запись в Kafka\n* чтение из Redis\n1. первый шаг\n2) второй шаг')

    // then
    expect(blocks).toEqual([
      {
        kind: 'bullets',
        items: [
          { marker: '-', text: 'запись в Kafka' },
          { marker: '*', text: 'чтение из Redis' },
        ],
      },
      {
        kind: 'numbers',
        items: [
          { marker: '1', text: 'первый шаг' },
          { marker: '2', text: 'второй шаг' },
        ],
      },
    ])
  })

  it('берёт код между ``` как есть, а незакрытый блок — до конца текста', () => {
    // when
    const closed = parseRichText('Схема:\n```text\nКлиент -> Kafka\n  - не список\n```\nИтог')
    const unclosed = parseRichText('```\nКлиент -> Redis')

    // then
    expect(closed).toEqual([
      { kind: 'paragraph', text: 'Схема:' },
      { kind: 'code', text: 'Клиент -> Kafka\n  - не список' },
      { kind: 'paragraph', text: 'Итог' },
    ])
    expect(unclosed).toEqual([{ kind: 'code', text: 'Клиент -> Redis' }])
  })

  it('выделяет жирный текст и код внутри строки', () => {
    // when
    const spans = parseInline('**A — CQRS**: пишем `SELECT FOR UPDATE` реже')

    // then
    expect(spans).toEqual([
      { kind: 'bold', text: 'A — CQRS' },
      { kind: 'text', text: ': пишем ' },
      { kind: 'code', text: 'SELECT FOR UPDATE' },
      { kind: 'text', text: ' реже' },
    ])
    expect(plainText('**A** и `B`')).toBe('A и B')
  })

  it('короткий однострочный вопрос выводится крупно, задача с вариантами — нет', () => {
    // when / then
    expect(isShortText('Что такое MVCC?')).toBe(true)
    expect(isShortText('Какой подход выбрать?\nA. CQRS\nB. Шардирование')).toBe(false)
    expect(isShortText('а'.repeat(141))).toBe(false)
  })

  it('отделяет первую строку вопроса от остального', () => {
    // when / then
    expect(splitFirstLine('Условие задачи\n\nA. CQRS\nB. Шардирование')).toEqual({
      first: 'Условие задачи',
      rest: 'A. CQRS\nB. Шардирование',
    })
    expect(splitFirstLine(' Что такое MVCC? ')).toEqual({ first: 'Что такое MVCC?', rest: '' })
  })
})
