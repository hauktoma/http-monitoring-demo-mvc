package cz.hauk.httpmonitoringdemo.endpoints

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MonitoredEndpointResultDeleteTask(
    private val monitoringResultRepo: MonitoringResultRepository
) {
    @Scheduled(fixedDelayString = "\${task.cleanup.delay.millis:1000}")
    fun cleanupTask() {
        LOG.info("Executing monitored result cleanup task.")
        while (doPerformDelete()) LOG.info("Records deleted, repeating delete task.")
        LOG.info("Monitored result cleanup task finished.")
    }

    @Transactional
    private fun doPerformDelete(): Boolean = monitoringResultRepo.findRecordsToDelete().let { recordsToDelete ->
        when (recordsToDelete.isEmpty()) {
            true -> {
                LOG.info("No monitored results to delete.")
                false
            }
            else -> {
                monitoringResultRepo.deleteByIds(recordsToDelete.map { it })
                LOG.info("Deleted ${recordsToDelete.count()} monitored results.")
                true
            }
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(MonitoredEndpointResultDeleteTask::class.java)
    }
}
