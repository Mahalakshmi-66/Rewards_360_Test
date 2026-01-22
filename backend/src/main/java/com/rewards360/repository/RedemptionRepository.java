
package com.rewards360.repository;

import com.rewards360.model.Redemption;
import com.rewards360.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RedemptionRepository extends JpaRepository<Redemption, Long> {
    List<Redemption> findByUser(User user);
}
