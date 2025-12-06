package trinhnv.jobOKO.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import trinhnv.jobOKO.domain.entity.User;
import trinhnv.jobOKO.domain.projection.UserDetailsProjections;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    User findByUserName(String userName);

    boolean existsByEmail(String email);

    boolean existsByUserName(String userName);

    @Query(
            "SELECT u.userId as userId, " +
                    "u.userName as userName, " +
                    "u.email as email, " +
                    "u.address as address, " +
                    "u.age as age, " +
                    "CAST(u.gender AS string) as gender, " +
                    "c.companyId as companyId, " +
                    "c.name as companyName, " +
                    "c.logo as companyLogo, " +
                    "c.address as companyAddress, " +
                    "c.description as companyDescription " +
                    "FROM User u LEFT JOIN companies c ON u.companyId = c.companyId " +
                    "WHERE u.userId = :userId"
    )
    UserDetailsProjections findUserDetailsById(@Param("userId") Long userId);

}
