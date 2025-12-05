package trinhnv.jobOKO.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import trinhnv.jobOKO.domain.entity.User;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    User findByUserName(String userName);

    boolean existsByEmail(String email);
    boolean existsByUserName(String userName);
}
