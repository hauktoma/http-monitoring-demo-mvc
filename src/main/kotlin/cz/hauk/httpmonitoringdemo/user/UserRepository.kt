package cz.hauk.httpmonitoringdemo.user

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : CrudRepository<UserDBO, UUID> {

    @Query("SELECT * FROM user_dbo WHERE api_key = :apiKey")
    fun findByApiKey(@Param("apiKey") apiKey: String): UserDBO?

}

data class UserDBO(
    @Id
    val id: UUID,
    val username: String,
    val email: String,
    val apiKey: String,
    @Version
    val version: Long
)
