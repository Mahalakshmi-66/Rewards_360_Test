package com.rewards360.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rewards360.dto.ClaimRequest;
import com.rewards360.dto.RedeemRequest;
import com.rewards360.model.Offer;
import com.rewards360.model.Redemption;
import com.rewards360.model.Transaction;
import com.rewards360.model.User;
import com.rewards360.repository.OfferRepository;
import com.rewards360.repository.RedemptionRepository;
import com.rewards360.repository.TransactionRepository;
import com.rewards360.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointsService {

    private final UserRepository userRepository;
    private final OfferRepository offerRepository;
    private final TransactionRepository transactionRepository;
    private final RedemptionRepository redemptionRepository;

    @Transactional
    public Transaction claimPoints(User user, ClaimRequest req) {
        // Build CLAIM transaction (same logic as in original controller)
        Transaction txn = Transaction.builder()
            .externalId("CLAIM-" + System.currentTimeMillis())
            .type("CLAIM")
            .pointsEarned(req.points())
            .pointsRedeemed(0)
            .store("Online")
            .date(LocalDate.now())
            .expiry(LocalDate.now().plusMonths(3))
            .note(req.note())
            .user(user)
            .build();

        // Update balance then persist
        user.getProfile().setPointsBalance(user.getProfile().getPointsBalance() + req.points());
        transactionRepository.save(txn);
        userRepository.save(user);

        return txn;
    }

    @Transactional
    public Optional<Redemption> redeemOffer(User user, RedeemRequest req) {
        // Load offer
        Offer offer = offerRepository.findById(req.offerId()).orElseThrow();

        // Insufficient points -> signal caller to return 400, matching original behavior
        if (user.getProfile().getPointsBalance() < offer.getCostPoints()) {
            return Optional.empty();
        }

        // Deduct points
        user.getProfile().setPointsBalance(
            user.getProfile().getPointsBalance() - offer.getCostPoints()
        );

        // Create REDEMPTION transaction
        Transaction txn = Transaction.builder()
            .externalId("RED-" + System.currentTimeMillis())
            .type("REDEMPTION")
            .pointsEarned(0)
            .pointsRedeemed(offer.getCostPoints())
            .store(req.store())
            .date(LocalDate.now())
            .note(offer.getTitle())
            .user(user)
            .build();
        transactionRepository.save(txn);

        // Create Redemption record
        Redemption red = Redemption.builder()
            .transactionId(txn.getExternalId())
            .confirmationCode("CONF-" + System.currentTimeMillis())
            .date(LocalDate.now())
            .costPoints(offer.getCostPoints())
            .offerTitle(offer.getTitle())
            .store(req.store())
            .user(user)
            .build();
        redemptionRepository.save(red);

        // Persist user balance
        userRepository.save(user);

        return Optional.of(red);
    }
}