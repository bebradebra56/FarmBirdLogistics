package com.farmbirdfs.logjfeiowewg.opf.presentation.di

import com.farmbirdfs.logjfeiowewg.opf.data.repo.FarmBirdLogisticsRepository
import com.farmbirdfs.logjfeiowewg.opf.data.shar.FarmBirdLogisticsSharedPreference
import com.farmbirdfs.logjfeiowewg.opf.data.utils.FarmBirdLogisticsPushToken
import com.farmbirdfs.logjfeiowewg.opf.data.utils.FarmBirdLogisticsSystemService
import com.farmbirdfs.logjfeiowewg.opf.domain.usecases.FarmBirdLogisticsGetAllUseCase
import com.farmbirdfs.logjfeiowewg.opf.presentation.pushhandler.FarmBirdLogisticsPushHandler
import com.farmbirdfs.logjfeiowewg.opf.presentation.ui.load.FarmBirdLogisticsLoadViewModel
import com.farmbirdfs.logjfeiowewg.opf.presentation.ui.view.FarmBirdLogisticsViFun
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val farmBirdLogisticsModule = module {
    factory { FarmBirdLogisticsPushHandler() }
    single { FarmBirdLogisticsRepository() }
    single { FarmBirdLogisticsSharedPreference(get()) }
    factory { FarmBirdLogisticsPushToken() }
    factory { FarmBirdLogisticsSystemService(get()) }
    factory {
        FarmBirdLogisticsGetAllUseCase(
            farmBirdLogisticsRepository = get(),
            farmBirdLogisticsSystemService = get(),
            farmBirdLogisticsPushToken = get()
        )
    }
    factory { FarmBirdLogisticsViFun(get()) }
    viewModel {
        FarmBirdLogisticsLoadViewModel(
            farmBirdLogisticsGetAllUseCase = get(),
            farmBirdLogisticsSharedPreference = get(),
            farmBirdLogisticsSystemService = get()
        )
    }
}