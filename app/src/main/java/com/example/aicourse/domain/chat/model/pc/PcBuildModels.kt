package com.example.aicourse.domain.chat.model.pc

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Промежуточная модель для десериализации JSON ответа от API
 * Используется в parseResponse для парсинга, затем конвертируется в PcBuildResponse
 */
@Serializable
internal data class PcBuildApiResponse(
    @SerialName("is_finished")
    val isFinished: Boolean,
    val question: String? = null,
    @SerialName("pc_build")
    val pcBuild: PcBuild? = null
)

/**
 * Полная сборка ПК
 */
@Serializable
data class PcBuild(
    @SerialName("build_name")
    val buildName: String,
    val reasoning: String,
    @SerialName("budget_currency")
    val budgetCurrency: String,
    @SerialName("total_price_approx")
    val totalPriceApprox: Int,
    val components: PcComponents
)

/**
 * Все компоненты ПК
 */
@Serializable
data class PcComponents(
    val cpu: PcComponent,
    val gpu: PcComponent,
    val ram: PcComponent,
    val motherboard: PcComponent,
    @SerialName("cooling_system")
    val coolingSystem: PcComponent,
    val storage: PcComponent,
    val psu: PcComponent,
    @SerialName("case")
    val caseComponent: PcComponent
)

/**
 * Универсальный компонент ПК
 * Используется для всех типов компонентов (CPU, GPU, RAM, и т.д.)
 *
 * @param label название компонента (например, "Процессор")
 * @param model модель компонента (например, "Intel Core i5-13600K")
 * @param specs технические характеристики (для большинства компонентов)
 * @param requirements требования (используется для RAM)
 * @param type тип компонента (используется для системы охлаждения: "Air Cooler" / "Liquid AIO")
 * @param note дополнительное примечание (опционально, используется для RAM)
 * @param priceApprox примерная цена
 */
@Serializable
data class PcComponent(
    val label: String,
    val model: String,
    val specs: Map<String, String>? = null,
    val requirements: Map<String, String>? = null,
    val type: String? = null,
    val note: String? = null,
    @SerialName("price_approx")
    val priceApprox: Int
)
