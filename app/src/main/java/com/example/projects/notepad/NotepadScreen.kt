package com.example.projects.notepad

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.projects.ui.theme.CalculatorTheme

object NotepadRoutes {
    const val NOTE_LIST = "note_list"
    const val NOTE_DETAIL = "note_detail"
}

@Composable
fun NotepadScreen(
    viewModel: NotepadViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val notepadNavController = rememberNavController()

    CalculatorTheme {
        NavHost(
            navController = notepadNavController,
            startDestination = NotepadRoutes.NOTE_LIST
        ) {
            composable(NotepadRoutes.NOTE_LIST) {
                NoteListScreen(
                    state = state,
                    onNoteClick = { noteId ->
                        viewModel.selectNote(noteId)
                        notepadNavController.navigate(NotepadRoutes.NOTE_DETAIL)
                    },
                    onAddNewNoteClick = {
                        viewModel.addNewNote()
                        notepadNavController.navigate(NotepadRoutes.NOTE_DETAIL)
                    },
                )
            }
            composable(NotepadRoutes.NOTE_DETAIL) {
                state.selectedNoteId?.let { noteId ->
                    key(noteId) {
                        NoteDetailScreen(
                            state = state,
                            viewModel = viewModel,
                            onBackClick = {
                                viewModel.clearSelectedNote()
                                notepadNavController.popBackStack()
                            },
                            onDeleteClick = {
                                viewModel.deleteNote(noteId)
                                notepadNavController.popBackStack()
                            }
                        )
                    }
                } ?: run {
                    notepadNavController.popBackStack(NotepadRoutes.NOTE_LIST, inclusive = true)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    state: NotepadState,
    onNoteClick: (String) -> Unit,
    onAddNewNoteClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notes", fontWeight = FontWeight.SemiBold) },
                actions = {
                    IconButton(onClick = onAddNewNoteClick) {
                        Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = "New Note")
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNewNoteClick) {
                Icon(Icons.Filled.Add, contentDescription = "Add new note")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.notes, key = { it.id }) { note ->
                NoteListItem(
                    note = note,
                    onClick = { onNoteClick(note.id) },
                )
            }
            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }
}


@Composable
fun NoteListItem(
    note: Note,
    onClick: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.elevatedCardElevation(2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.title.ifEmpty { "Untitled" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = note.content.ifEmpty { "Empty note" },
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = note.formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    state: NotepadState,
    viewModel: NotepadViewModel,
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val currentNote = state.currentNote
    if (currentNote == null) {
        onBackClick()
        return
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = viewModel::saveNote) {
                            Icon(Icons.Filled.Save, contentDescription = "Save")
                        }
                        IconButton(onClick = viewModel::undo) {
                            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                        }
                        IconButton(onClick = viewModel::redo) {
                            Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo")
                        }
                        IconButton(onClick = onDeleteClick) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete Note")
                        }
                    }
                )

                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    BasicTextField(
                        value = state.currentNoteTitle,
                        onValueChange = viewModel::onNoteTitleChanged,
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            if (state.currentNoteTitle.isEmpty()) {
                                Text(
                                    text = "Title",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                NotepadControls(
                    state = state,
                    viewModel = viewModel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        BasicTextField(
            value = state.currentNoteContent,
            onValueChange = viewModel::onNoteContentChanged,
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = state.currentNoteFontSize,
                fontWeight = if (state.currentNoteIsBold) FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (state.currentNoteIsItalic) FontStyle.Italic else FontStyle.Normal
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                if (state.currentNoteContent.isEmpty()) {
                    Text(
                        text = "Start writing here...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                innerTextField()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun NotepadControls(
    state: NotepadState,
    viewModel: NotepadViewModel,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        LazyRow(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            item {
                StyleToggleButton(
                    icon = Icons.Filled.FormatBold,
                    contentDescription = "Bold",
                    isSelected = state.currentNoteIsBold,
                    onClick = viewModel::toggleBold
                )
            }
            item {
                StyleToggleButton(
                    icon = Icons.Filled.FormatItalic,
                    contentDescription = "Italic",
                    isSelected = state.currentNoteIsItalic,
                    onClick = viewModel::toggleItalic
                )
            }
            item {
                TextButton(
                    onClick = viewModel::resetStyle,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text("Normal", style = MaterialTheme.typography.labelMedium)
                }
            }

            item { CustomVerticalDivider(modifier = Modifier
                .padding(horizontal = 4.dp)
                .height(24.dp)) }

            item {
                IconButton(onClick = viewModel::cut) {
                    Icon(Icons.Filled.ContentCut, contentDescription = "Cut")
                }
            }
            item {
                IconButton(onClick = viewModel::copy) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy")
                }
            }
            item {
                IconButton(onClick = viewModel::paste) {
                    Icon(Icons.Filled.ContentPaste, contentDescription = "Paste")
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            CustomVerticalDivider(modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxHeight(0.8f))

            IconButton(
                onClick = { viewModel.changeFontSize(false) },
                enabled = state.currentNoteFontSize.value > 12f,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Filled.Remove, contentDescription = "Decrease Font Size", modifier = Modifier.size(20.dp))
            }
            Text(
                text = "${state.currentNoteFontSize.value.toInt()}sp",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            IconButton(
                onClick = { viewModel.changeFontSize(true) },
                enabled = state.currentNoteFontSize.value < 30f,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Increase Font Size", modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun StyleToggleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
        )
    }
}

@Composable
fun CustomVerticalDivider(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.outlineVariant) {
    HorizontalDivider(
        color = color,
        modifier = modifier
            .width(1.dp)
            .fillMaxHeight()
    )
}
