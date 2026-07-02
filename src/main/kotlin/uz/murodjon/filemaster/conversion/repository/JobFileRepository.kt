package uz.murodjon.filemaster.conversion.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uz.murodjon.filemaster.conversion.model.JobFile

@Repository
interface JobFileRepository : JpaRepository<JobFile, Long> {

}
