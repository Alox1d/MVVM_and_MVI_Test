package ru.otus.tomvi.presentation

sealed class UiState {
    object Loading : UiState()
    class Error(val error: String) : UiState()
    class Success(val characters: List<CharacterState> = emptyList()) : UiState()
}

data class CharacterState(
    val id: Long = 0L,
    val name: String = "",
    val image: String = "",
    val isFavorite: Boolean = false,
)
