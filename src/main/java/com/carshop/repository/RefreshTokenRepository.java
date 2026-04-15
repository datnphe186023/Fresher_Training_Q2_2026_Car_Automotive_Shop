package com.carshop.repository;

import com.carshop.entity.RefreshToken;
import com.carshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for RefreshToken entity operations.
 * Provides CRUD operations and custom query methods for token management.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    
    /**
     * Find a refresh token by its token string.
     * Used for token validation during refresh operations.
     *
     * @param token the token string to search for
     * @return Optional containing the refresh token if found, empty otherwise
     */
    Optional<RefreshToken> findByToken(String token);
    
    /**
     * Delete all refresh tokens associated with a user.
     * Used during logout to invalidate all user sessions.
     *
     * @param user the user whose tokens should be deleted
     */
    void deleteByUser(User user);
}
