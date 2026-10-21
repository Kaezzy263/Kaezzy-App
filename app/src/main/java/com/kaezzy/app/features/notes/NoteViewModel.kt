package com.kaezzy.app.features.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaezzy.app.core.database.entity.NoteEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _isArchivedView = MutableStateFlow(false)
    val isArchivedView: StateFlow<Boolean> = _isArchivedView.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _notes = MutableStateFlow<List<NoteEntity>>(emptyList())
    val notes: StateFlow<List<NoteEntity>> = _notes.asStateFlow()

    init {
        observeNotes()
    }

    fun toggleArchivedView() {
        _isArchivedView.value = !_isArchivedView.value
        observeNotes()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        observeNotes()
    }

    fun saveNote(title: String, content: String, id: String? = null) {
        viewModelScope.launch {
            repository.saveNote(title, content, id)
        }
    }

    fun togglePin(note: NoteEntity) {
        viewModelScope.launch {
            repository.togglePinNote(note)
        }
    }

    fun toggleArchive(note: NoteEntity) {
        viewModelScope.launch {
            repository.toggleArchiveNote(note)
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    private fun observeNotes() {
        viewModelScope.launch {
            val flow = when {
                _searchQuery.value.isNotBlank() -> repository.searchNotes(_searchQuery.value)
                _isArchivedView.value -> repository.getArchivedNotes()
                else -> repository.getActiveNotes()
            }
            flow.collect { _notes.value = it }
        }
    }
}
