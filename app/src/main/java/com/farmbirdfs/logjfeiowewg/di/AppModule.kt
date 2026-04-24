package com.farmbirdfs.logjfeiowewg.di

import androidx.room.Room
import com.farmbirdfs.logjfeiowewg.data.local.AppDatabase
import com.farmbirdfs.logjfeiowewg.data.preferences.UserPreferences
import com.farmbirdfs.logjfeiowewg.data.repository.*
import com.farmbirdfs.logjfeiowewg.presentation.screens.arrival.ArrivalViewModel
import com.farmbirdfs.logjfeiowewg.presentation.screens.birds.AddBirdGroupViewModel
import com.farmbirdfs.logjfeiowewg.presentation.screens.birds.BirdGroupsViewModel
import com.farmbirdfs.logjfeiowewg.presentation.screens.calendar.CalendarViewModel
import com.farmbirdfs.logjfeiowewg.presentation.screens.conditions.TransportConditionsViewModel
import com.farmbirdfs.logjfeiowewg.presentation.screens.containers.AddContainerViewModel
import com.farmbirdfs.logjfeiowewg.presentation.screens.containers.ContainersViewModel
import com.farmbirdfs.logjfeiowewg.presentation.screens.dashboard.DashboardViewModel
import com.farmbirdfs.logjfeiowewg.presentation.screens.feeding.AddFeedRecordViewModel
import com.farmbirdfs.logjfeiowewg.presentation.screens.feeding.FeedingViewModel
import com.farmbirdfs.logjfeiowewg.presentation.screens.health.AddHealthRecordViewModel
import com.farmbirdfs.logjfeiowewg.presentation.screens.health.HealthCheckViewModel
import com.farmbirdfs.logjfeiowewg.presentation.screens.history.ActivityHistoryViewModel
import com.farmbirdfs.logjfeiowewg.presentation.screens.housing.AddHousingViewModel
import com.farmbirdfs.logjfeiowewg.presentation.screens.housing.HousingViewModel
import com.farmbirdfs.logjfeiowewg.presentation.screens.inventory.InventoryViewModel
import com.farmbirdfs.logjfeiowewg.presentation.screens.loading.LoadingDetailsViewModel
import com.farmbirdfs.logjfeiowewg.presentation.screens.loading.LoadingViewModel
import com.farmbirdfs.logjfeiowewg.presentation.screens.profile.ProfileViewModel
import com.farmbirdfs.logjfeiowewg.presentation.screens.reports.ReportsViewModel
import com.farmbirdfs.logjfeiowewg.presentation.screens.route.RouteViewModel
import com.farmbirdfs.logjfeiowewg.presentation.screens.settings.SettingsViewModel
import com.farmbirdfs.logjfeiowewg.presentation.screens.tasks.AddTaskViewModel
import com.farmbirdfs.logjfeiowewg.presentation.screens.tasks.TasksViewModel
import com.farmbirdfs.logjfeiowewg.presentation.screens.transport.CreateTransportViewModel
import com.farmbirdfs.logjfeiowewg.presentation.screens.transport.TransportPlansViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "farm_bird_db"
        ).build()
    }

    single { get<AppDatabase>().transportPlanDao() }
    single { get<AppDatabase>().birdGroupDao() }
    single { get<AppDatabase>().containerDao() }
    single { get<AppDatabase>().loadingRecordDao() }
    single { get<AppDatabase>().routeStopDao() }
    single { get<AppDatabase>().transportConditionDao() }
    single { get<AppDatabase>().housingDao() }
    single { get<AppDatabase>().feedRecordDao() }
    single { get<AppDatabase>().healthRecordDao() }
    single { get<AppDatabase>().taskDao() }
    single { get<AppDatabase>().inventoryItemDao() }
    single { get<AppDatabase>().activityLogDao() }

    single { UserPreferences(androidContext()) }

    single { ActivityLogRepository(get()) }
    single { TransportRepository(get(), get()) }
    single { BirdGroupRepository(get(), get()) }
    single { ContainerRepository(get(), get()) }
    single { LoadingRepository(get(), get()) }
    single { RouteRepository(get(), get()) }
    single { ConditionRepository(get(), get()) }
    single { HousingRepository(get(), get()) }
    single { FeedingRepository(get(), get()) }
    single { HealthRepository(get(), get()) }
    single { TaskRepository(get(), get()) }
    single { InventoryRepository(get(), get()) }

    viewModel { DashboardViewModel(get(), get(), get(), get(), get()) }
    viewModel { TransportPlansViewModel(get()) }
    viewModel { (transportId: Long) -> CreateTransportViewModel(get(), transportId) }
    viewModel { BirdGroupsViewModel(get()) }
    viewModel { (groupId: Long) -> AddBirdGroupViewModel(get(), groupId) }
    viewModel { ContainersViewModel(get()) }
    viewModel { (containerId: Long) -> AddContainerViewModel(get(), containerId) }
    viewModel { (transportId: Long) -> LoadingViewModel(get(), get(), get(), transportId) }
    viewModel { (transportId: Long) -> LoadingDetailsViewModel(get(), get(), get(), transportId) }
    viewModel { (transportId: Long) -> RouteViewModel(get(), get(), transportId) }
    viewModel { (transportId: Long) -> TransportConditionsViewModel(get(), transportId) }
    viewModel { (transportId: Long) -> ArrivalViewModel(get(), transportId) }
    viewModel { HousingViewModel(get()) }
    viewModel { (housingId: Long) -> AddHousingViewModel(get(), housingId) }
    viewModel { FeedingViewModel(get(), get()) }
    viewModel { (recordId: Long) -> AddFeedRecordViewModel(get(), get(), recordId) }
    viewModel { HealthCheckViewModel(get(), get()) }
    viewModel { (recordId: Long) -> AddHealthRecordViewModel(get(), get(), recordId) }
    viewModel { InventoryViewModel(get()) }
    viewModel { CalendarViewModel(get(), get(), get()) }
    viewModel { TasksViewModel(get()) }
    viewModel { (taskId: Long) -> AddTaskViewModel(get(), taskId) }
    viewModel { ReportsViewModel(get(), get(), get(), get()) }
    viewModel { ActivityHistoryViewModel(get()) }
    viewModel { ProfileViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
}
