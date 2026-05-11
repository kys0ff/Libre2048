package off.kys.libre2048.di

import off.kys.libre2048.data.repository.GameRepository
import off.kys.libre2048.ui.game.GameViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::GameRepository)
    factoryOf(::GameViewModel)
}