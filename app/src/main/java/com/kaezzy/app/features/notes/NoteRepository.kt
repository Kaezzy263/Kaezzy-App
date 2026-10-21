package com.kaezzy.app.features.notes

import com.kaezzy.app.core.database.dao.NoteDao
import com.kaezzy.app.core.database.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    fun getActiveNotes(): Flow<List<NoteEntity>> = noteDao.getActiveNotes()
    fun getArchivedNotes(): Flow<List<NoteEntity>> = noteDao.getArchivedNotes()
    fun searchNotes(query: String): Flow<List<NoteEntity>> = noteDao.searchNotes(query)

    suspend fun getNoteById(id: String): NoteEntity? = noteDao.getNoteById(id)

    suspend fun saveNote(title: String, content: String, existingId: String? = null) {
        val nowMs = System.currentTimeMillis()
        val finalTitle = title.trim().ifEmpty {
            content.trim().take(40).ifEmpty { "Catatan Tanpa Judul" }
        }

        if (existingId != null) {
            val existing = noteDao.getNoteById(existingId)
            if (existing != null) {
                noteDao.updateNote(existing.copy(title = finalTitle, content = content, updatedAt = nowMs))
                return
            }
        }

        noteDao.insertNote(
            NoteEntity(
                title = finalTitle,
                content = content,
                createdAt = nowMs,
                updatedAt = nowMs
            )
        )
    }

    suspend fun togglePinNote(note: NoteEntity) {
        noteDao.updateNote(note.copy(isPinned = if (note.isPinned == 1) 0 else 1, updatedAt = System.currentTimeMillis()))
    }

    suspend fun toggleArchiveNote(note: NoteEntity) {
        noteDao.updateNote(note.copy(isArchived = if (note.isArchived == 1) 0 else 1, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteNote(note: NoteEntity) {
        noteDao.deleteNote(note)
    }
}
