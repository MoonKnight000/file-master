package uz.murodjon.filemaster.conversion.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.data.domain.Sort
import uz.murodjon.filemaster.common.JobStatus
import uz.murodjon.filemaster.conversion.enums.ConversionFilterOrder
import uz.murodjon.filemaster.pageable.FilterInterface

data class ConversionFilterRequest(
    @field:Min(1, message = "Page must be greater than or equal to 1")
    override val page: Int = 1,
    @field:Min(1, message = "Size must be greater than or equal to 1")
    @field:Max(100, message = "Size must be less than or equal to 100")
    override val size: Int = 10,
    override val orders: LinkedHashMap<ConversionFilterOrder, Sort.Direction> = linkedMapOf(ConversionFilterOrder.CREATED_TIMESTAMP to Sort.Direction.DESC),
    val status: List<JobStatus> = JobStatus.entries
) : FilterInterface<ConversionFilterOrder>