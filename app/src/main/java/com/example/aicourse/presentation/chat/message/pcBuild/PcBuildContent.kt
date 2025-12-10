package com.example.aicourse.presentation.chat.message.pcBuild

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aicourse.domain.chat.promt.pc.PcBuild
import com.example.aicourse.domain.chat.promt.pc.PcComponent
import com.example.aicourse.domain.chat.promt.pc.PcComponents
import com.example.aicourse.ui.theme.AiCourseTheme

/**
 * Отображает содержимое сборки ПК
 */
@Composable
fun PcBuildContent(pcBuild: PcBuild) {
    Column {
        Text(
            text = pcBuild.buildName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Companion.Bold,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )

        Spacer(modifier = Modifier.Companion.height(8.dp))

        Text(
            text = pcBuild.reasoning,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.Companion.height(12.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.Companion.height(12.dp))

        Text(
            text = "💰 Общая стоимость: ~${pcBuild.totalPriceApprox} ${pcBuild.budgetCurrency}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Companion.SemiBold,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )

        Spacer(modifier = Modifier.Companion.height(12.dp))

        ComponentRow("💻", pcBuild.components.cpu.label, pcBuild.components.cpu.model, pcBuild.components.cpu.priceApprox)
        ComponentRow("🎮", pcBuild.components.gpu.label, pcBuild.components.gpu.model, pcBuild.components.gpu.priceApprox)
        ComponentRow("🧠", pcBuild.components.ram.label, pcBuild.components.ram.model, pcBuild.components.ram.priceApprox)
        ComponentRow(
            "🔧",
            pcBuild.components.motherboard.label,
            pcBuild.components.motherboard.model,
            pcBuild.components.motherboard.priceApprox
        )
        ComponentRow(
            "❄️",
            pcBuild.components.coolingSystem.label,
            pcBuild.components.coolingSystem.model,
            pcBuild.components.coolingSystem.priceApprox
        )
        ComponentRow("💾", pcBuild.components.storage.label, pcBuild.components.storage.model, pcBuild.components.storage.priceApprox)
        ComponentRow("⚡", pcBuild.components.psu.label, pcBuild.components.psu.model, pcBuild.components.psu.priceApprox)
        ComponentRow(
            "📦",
            pcBuild.components.caseComponent.label,
            pcBuild.components.caseComponent.model,
            pcBuild.components.caseComponent.priceApprox
        )
    }
}

/**
 * Строка с компонентом
 */
@Composable
private fun ComponentRow(emoji: String, label: String, model: String, price: Int) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "$emoji $label",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = model,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
        Text(
            text = "~$price ₽",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f)
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
fun PcBuildContentPreview() {
    AiCourseTheme {
        Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier.padding(16.dp) // Немного отступа для красоты превью
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                PcBuildContent(pcBuild = samplePcBuild)
            }
        }
    }
}

val samplePcBuild = PcBuild(
    buildName = "Оптимальный Гейминг 1080p",
    reasoning = "Сбалансированная сборка для запуска современных AAA-игр на высоких настройках графики в Full HD. Отличное соотношение цена/производительность.",
    budgetCurrency = "RUB",
    totalPriceApprox = 78500,
    components = PcComponents(
        cpu = PcComponent(
            label = "Процессор",
            model = "Intel Core i5-12400F",
            priceApprox = 12500
        ),
        gpu = PcComponent(
            label = "Видеокарта",
            model = "NVIDIA GeForce RTX 3060 12GB",
            priceApprox = 28000
        ),
        ram = PcComponent(
            label = "ОЗУ",
            model = "Kingston FURY Beast Black [KF432C16BBK2/16] 16 ГБ",
            priceApprox = 4500
        ),
        motherboard = PcComponent(
            label = "Материнская плата",
            model = "GIGABYTE B660M DS3H DDR4",
            priceApprox = 10500
        ),
        coolingSystem = PcComponent(
            label = "Охлаждение",
            model = "Deepcool AK400",
            priceApprox = 2500
        ),
        storage = PcComponent(
            label = "Накопитель",
            model = "1000 ГБ SSD M.2 накопитель Samsung 970 EVO Plus",
            priceApprox = 8500
        ),
        psu = PcComponent(
            label = "Блок питания",
            model = "Deepcool PK650D 650W",
            priceApprox = 5500
        ),
        caseComponent = PcComponent(
            label = "Корпус",
            model = "Zalman S2 Black",
            priceApprox = 4500
        )
    )
)