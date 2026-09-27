import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ChevronLeft, ChevronRight } from 'lucide-react'
import { useCreateTopic } from '@/modules/topic'
import { InlineError } from '@/shared'

/** Экран создания темы: название и необязательное описание. */
export function CreateTopicPage() {
  const navigate = useNavigate()
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const create = useCreateTopic((topic) => navigate(`/topics/${topic.id}`))
  return (
    <div className="content-wrap narrow">
      <button className="back-link" onClick={() => navigate(-1)}>
        <ChevronLeft size={17} /> Назад
      </button>
      <div className="form-page">
        <div className="eyebrow">Новая тема</div>
        <h1>С чего начнём?</h1>
        <p className="muted intro">Создайте пространство для материала, который хотите запомнить.</p>
        {create.error && <InlineError message={create.error.message} />}
        <form
          className="form-stack"
          onSubmit={(e) => {
            e.preventDefault()
            create.mutate({ title: title.trim(), description: description.trim() })
          }}
        >
          <label>
            Название темы
            <input
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Например, Spring Security"
              maxLength={200}
              autoFocus
              required
            />
          </label>
          <label>
            Описание <span className="optional">необязательно</span>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="О чём эта тема?"
              rows={4}
              maxLength={2000}
            />
          </label>
          <button className="button button-primary" disabled={create.isPending}>
            {create.isPending ? 'Создаём…' : 'Создать тему'} <ChevronRight size={17} />
          </button>
        </form>
      </div>
    </div>
  )
}
