package ru.netology.cloudStorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.netology.cloudStorage.entity.AuthToken;
import ru.netology.cloudStorage.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    Optional<AuthToken> findByToken(String token);

    int deleteByToken(String token);

    int deleteByUser(User user);

    List<AuthToken> findByExpiresAtBefore(LocalDateTime dateTime);

    boolean existsByUserAndExpiresAtAfter(User user, LocalDateTime dateTime);

    List<AuthToken> findByUserIdOrderByCreatedAtAsc(Long userId);

    @Query("SELECT at.user.id, COUNT(at) FROM AuthToken at GROUP BY at.user.id HAVING COUNT(at) > 5")
    List<Object[]> findUsersWithTokenCount();

    @Query("SELECT at FROM AuthToken at JOIN FETCH at.user WHERE at.token = :token")
    Optional<AuthToken> findByTokenWithUser(@Param("token") String token);
}