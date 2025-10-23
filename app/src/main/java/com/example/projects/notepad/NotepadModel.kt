package com.example.projects.notepad

import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

data class Note(
    val id: String = UUID.randomUUID().toString(),
    var title: String = "",
    var content: String = "",
    val creationTime: Date = Date(),
    var lastModifiedTime: Date = Date(),
) {
    private val formatter = SimpleDateFormat("MM/dd/yy HH:mm", Locale.getDefault())
    val formattedDate: String
        get() = formatter.format(lastModifiedTime)
}

data class NotepadState(
    val notes: List<Note> = emptyList(),
    val selectedNoteId: String? = null,
    val currentNoteTitle: String = "",
    val currentNoteContent: String = "",
    val currentNoteFontSize: TextUnit = 16.sp,
    val currentNoteIsBold: Boolean = false,
    val currentNoteIsItalic: Boolean = false,
) {
    val currentNote: Note?
        get() = notes.firstOrNull { it.id == selectedNoteId }
}
