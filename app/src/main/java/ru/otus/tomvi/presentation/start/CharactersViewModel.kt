package ru.otus.tomvi.presentation.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.otus.tomvi.data.CharactersRepository
import ru.otus.tomvi.data.FavoritesRepository
import ru.otus.tomvi.presentation.CharacterStateFactory
import ru.otus.tomvi.presentation.UiState

class CharactersViewModel(
    private val charactersRepository: CharactersRepository,
    private val favoritesRepository: FavoritesRepository,
    private val stateFactory: CharacterStateFactory,
) : ViewModel() {

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Loading)
    val state: StateFlow<UiState> = _uiState.asStateFlow()

    private val searchFlow = MutableSharedFlow<String>()

    init {
        requestCharacters()
        subscribeOnSearchFlow()
    }

    private fun subscribeOnSearchFlow() {
        searchFlow
            .debounce(500)
            .onEach { searchName ->
                if (searchName.isBlank()) requestCharacters()
                if (searchName.length > 3) requestCharacters(searchName)
            }
            .launchIn(viewModelScope)
    }

    fun refresh() {
        requestCharacters()
    }

    fun refresh(searchName: String) {
        viewModelScope.launch {
            searchFlow.emit(searchName)
        }
    }

    private fun requestCharacters(name: String? = null) {
        combine(
            charactersRepository.consumeCharacters(name),
            favoritesRepository.consumeFavorites(),
        ) { characters, favorites -> stateFactory.create(characters, favorites) }
            .onStart {
                _uiState.update { _ ->
                    UiState.Loading
                }
            }
            .onEach { charactersState ->
                _uiState.update { currentState ->
                    UiState.Success(characters = charactersState)
                }
            }
            .catch {
                println("DBG: $it")
                _uiState.update { _ ->
                    UiState.Error("Error")
                }
            }
            .launchIn(viewModelScope)
    }

    fun addToFavorites(id: Long) {
        viewModelScope.launch {
            favoritesRepository.addToFavorites(id)
        }
    }

    fun removeFromFavorites(id: Long) {
        viewModelScope.launch {
            favoritesRepository.removeFromFavorites(id)
        }
    }

//    fun errorHasShown() {
//        _uiState.update { currentState ->
//            currentState.copy(hasError = false)
//        }
//    }
}
