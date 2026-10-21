package com.kaezzy.app.features.skills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaezzy.app.core.database.entity.SkillEntity
import com.kaezzy.app.core.database.entity.SkillEntryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SkillViewModel(private val repository: SkillRepository) : ViewModel() {

    private val _skills = MutableStateFlow<List<SkillEntity>>(emptyList())
    val skills: StateFlow<List<SkillEntity>> = _skills.asStateFlow()

    private val _selectedSkill = MutableStateFlow<SkillEntity?>(null)
    val selectedSkill: StateFlow<SkillEntity?> = _selectedSkill.asStateFlow()

    private val _entries = MutableStateFlow<List<SkillEntryEntity>>(emptyList())
    val entries: StateFlow<List<SkillEntryEntity>> = _entries.asStateFlow()

    init {
        observeSkills()
    }

    fun selectSkill(skill: SkillEntity?) {
        _selectedSkill.value = skill
        if (skill != null) {
            viewModelScope.launch {
                repository.getEntriesForSkill(skill.id).collect {
                    _entries.value = it
                }
            }
        }
    }

    fun addSkill(name: String, category: String) {
        viewModelScope.launch {
            repository.addSkill(name, category)
        }
    }

    fun addSkillEntry(
        skillId: String,
        entryType: String,
        title: String,
        content: String,
        codeLanguage: String? = null,
        referenceUrl: String? = null
    ) {
        viewModelScope.launch {
            repository.addSkillEntry(skillId, entryType, title, content, codeLanguage, referenceUrl)
        }
    }

    fun markStudiedToday(skillId: String) {
        viewModelScope.launch {
            repository.markStudiedToday(skillId)
        }
    }

    fun updateSkillStatus(skill: SkillEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateSkillStatus(skill, newStatus)
        }
    }

    fun deleteSkill(skill: SkillEntity) {
        viewModelScope.launch {
            repository.deleteSkill(skill)
            if (_selectedSkill.value?.id == skill.id) {
                _selectedSkill.value = null
            }
        }
    }

    fun deleteSkillEntry(entry: SkillEntryEntity) {
        viewModelScope.launch {
            repository.deleteSkillEntry(entry)
        }
    }

    private fun observeSkills() {
        viewModelScope.launch {
            repository.getAllSkills().collect {
                _skills.value = it
            }
        }
    }
}
