package com.example.projects.notepad

import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Date
import java.util.Calendar

class NotepadViewModel : ViewModel() {

    private val _state = MutableStateFlow(NotepadState(
        notes = getSampleNotes()
    ))
    val state: StateFlow<NotepadState> = _state.asStateFlow()

    private fun getOffsetDate(offsetAmount: Int, field: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.add(field, offsetAmount)
        return calendar.time
    }

    fun addNewNote() {
        val newNote = Note(title = "", content = "")
        _state.update {
            it.copy(
                notes = listOf(newNote) + it.notes,
                selectedNoteId = newNote.id,
                currentNoteTitle = newNote.title,
                currentNoteContent = newNote.content,
                currentNoteFontSize = 16.sp,
                currentNoteIsBold = false,
                currentNoteIsItalic = false
            )
        }
    }

    fun selectNote(noteId: String) {
        _state.update { currentState ->
            val note = currentState.notes.firstOrNull { n -> n.id == noteId }

            if (note == null) {
                return@update currentState.copy(selectedNoteId = null)
            }

            currentState.copy(
                selectedNoteId = noteId,
                currentNoteTitle = note.title,
                currentNoteContent = note.content,
                currentNoteFontSize = 16.sp,
                currentNoteIsBold = false,
                currentNoteIsItalic = false
            )
        }
    }

    fun updateNote(noteId: String, newTitle: String, newContent: String) {
        _state.update { currentState ->
            val updatedNotes = currentState.notes.map { note ->
                if (note.id == noteId) {
                    note.copy(
                        title = newTitle.ifBlank { "New Note" },
                        content = newContent,
                        lastModifiedTime = Date()
                    )
                } else note
            }.sortedByDescending { it.lastModifiedTime }

            currentState.copy(notes = updatedNotes)
        }
    }

    fun deleteNote(noteId: String) {
        _state.update {
            it.copy(
                notes = it.notes.filter { note -> note.id != noteId },
                selectedNoteId = if (it.selectedNoteId == noteId) null else it.selectedNoteId
            )
        }
    }

    fun clearSelectedNote() {
        _state.value.selectedNoteId?.let { noteId ->
            updateNote(noteId, _state.value.currentNoteTitle, _state.value.currentNoteContent)
        }
        _state.update { it.copy(selectedNoteId = null) }
    }

    fun onNoteTitleChanged(newTitle: String) = _state.update { it.copy(currentNoteTitle = newTitle) }
    fun onNoteContentChanged(newContent: String) = _state.update { it.copy(currentNoteContent = newContent) }
    fun toggleBold() = _state.update { it.copy(currentNoteIsBold = !it.currentNoteIsBold) }
    fun toggleItalic() = _state.update { it.copy(currentNoteIsItalic = !it.currentNoteIsItalic) }
    fun changeFontSize(increase: Boolean) {
        _state.update {
            val currentSize = it.currentNoteFontSize.value
            val newSize = if (increase) currentSize + 2 else currentSize - 2
            it.copy(currentNoteFontSize = newSize.coerceIn(12f, 30f).sp)
        }
    }
    fun resetStyle() = _state.update { it.copy(currentNoteIsBold = false, currentNoteIsItalic = false) }

    fun saveNote() { }
    fun undo() { }
    fun redo() { }
    fun cut() { }
    fun copy() { }
    fun paste() { }


    private fun getSampleNotes(): List<Note> {
        val loremIpsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit."

        return listOf(
            Note(
                title = "Note 1",
                content = loremIpsum,
                lastModifiedTime = getOffsetDate(-10, Calendar.MINUTE)
            ),
            Note(
                title = "Note 2",
                content = loremIpsum,
                lastModifiedTime = getOffsetDate(-30, Calendar.MINUTE)
            ),
            Note(
                title = "Note 3",
                content = loremIpsum,
                lastModifiedTime = getOffsetDate(-1, Calendar.HOUR_OF_DAY)
            ),
        ).sortedByDescending { it.lastModifiedTime }
    }
}
