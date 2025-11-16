package trinhnv.springRestfull.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import trinhnv.springRestfull.domain.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUserName(String userName);
}
