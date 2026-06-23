package com.example.db

import kotlinx.coroutines.flow.Flow

class AppRepository(private val appDao: AppDao) {

    val allGames: Flow<List<GameEntity>> = appDao.getAllGames()

    suspend fun insertGame(game: GameEntity) {
        appDao.insertGame(game)
    }

    suspend fun deleteGame(id: Int) {
        appDao.deleteGameById(id)
    }

    val allMessages: Flow<List<MessageEntity>> = appDao.getAllMessages()

    suspend fun insertMessage(msg: MessageEntity) {
        appDao.insertMessage(msg)
    }

    suspend fun clearMessages() {
        appDao.clearMessages()
    }

    val allMarketItems: Flow<List<MarketItemEntity>> = appDao.getAllMarketItems()

    suspend fun insertMarketItem(item: MarketItemEntity) {
        appDao.insertMarketItem(item)
    }

    suspend fun insertMarketItems(items: List<MarketItemEntity>) {
        appDao.insertMarketItems(items)
    }

    val userProfile: Flow<UserProfileEntity?> = appDao.getUserProfileFlow()

    suspend fun getUserProfileDirect(): UserProfileEntity? {
        return appDao.getUserProfileDirect()
    }

    suspend fun saveUserProfile(profile: UserProfileEntity) {
        appDao.insertUserProfile(profile)
    }
}
