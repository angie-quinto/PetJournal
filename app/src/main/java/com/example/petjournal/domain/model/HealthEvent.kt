package com.example.petjournal.domain.model

data class HealthEvent(
    val id: String,
    val petId: String,
    val type: String,
    val title: String,
    val eventDate: String,
    val eventTime: String?,
    val notes: String?,
    val providerName: String?,
    val diagnosisText: String?,
    val nextDueDate: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
    val vaccinationDetail: VaccinationDetail? = null,
    val dewormingDetail: DewormingDetail? = null,
    val medicationDetail: MedicationDetail? = null,
    val vetVisitDetail: VetVisitDetail? = null,
    val symptomDetail: SymptomDetail? = null
)

data class VaccinationDetail(
    val vaccineName: String,
    val doseDescription: String?,
    val batchNumber: String?,
    val provider: String?
)

data class DewormingDetail(
    val productName: String,
    val activeIngredient: String?,
    val dose: String?,
    val weightAtAdministrationGrams: Int?
)

data class MedicationDetail(
    val medicationName: String,
    val instructions: String,
    val startDate: String,
    val endDate: String?,
    val status: String
)

data class VetVisitDetail(
    val clinicName: String,
    val reason: String,
    val examinationNotes: String?,
    val followUpInstructions: String?
)

data class SymptomDetail(
    val description: String,
    val severity: String,
    val onset: String?,
    val resolution: String?
)

data class HealthEventInput(
    val petId: String,
    val type: String,
    val title: String,
    val eventDate: String,
    val id: String? = null,
    val eventTime: String? = null,
    val notes: String? = null,
    val providerName: String? = null,
    val diagnosisText: String? = null,
    val nextDueDate: String? = null,
    val vaccinationDetail: VaccinationDetail? = null,
    val dewormingDetail: DewormingDetail? = null,
    val medicationDetail: MedicationDetail? = null,
    val vetVisitDetail: VetVisitDetail? = null,
    val symptomDetail: SymptomDetail? = null
)
