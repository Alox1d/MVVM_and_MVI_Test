package ru.otus.tomvi.data

import android.accounts.NetworkErrorException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CharactersRepository(
    private val api: RAMRetrofitService
) {
    private var count = 0
    fun consumeCharacters(name: String? = null): Flow<List<RaMCharacter>> = flow {
        val characters = api.getCharacters(page = 0, name = name).body()?.results
            ?.map { dto ->
                RaMCharacter(
                    id = dto.id,
                    name = dto.name,
                    image = dto.image,
                )
            } ?: throw NetworkErrorException("sss")
        //throwTestExceptionOnFirstCall()
        emit(characters)
    }

    private fun throwTestExceptionOnFirstCall() {
        if (count == 0) {
            count++
            throw throw NetworkErrorException("sss")
        }
    }
}
