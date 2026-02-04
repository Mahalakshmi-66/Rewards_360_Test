package com.rewards360.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
import com.rewards360.service.PointsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin
public class UserController {

    private final UserRepository userRepository;
    private final OfferRepository offerRepository;
    private final TransactionRepository transactionRepository;
    private final RedemptionRepository redemptionRepository;

    // NEW: inject PointsService
    private final PointsService pointsService;

    private User currentUser(Authentication auth){
        return userRepository.findByEmail(auth.getName()).orElseThrow();
    }

    @GetMapping("/me")
    public ResponseEntity<User> me(Authentication auth){
        return ResponseEntity.ok(currentUser(auth));
    }

    @GetMapping("/offers")
    public ResponseEntity<List<Offer>> offers(){
        return ResponseEntity.ok(offerRepository.findAll());
    }

    // CHANGED: delegate to PointsService
    @PostMapping("/claim")
    public ResponseEntity<Transaction> claim(@RequestBody ClaimRequest req, Authentication auth){
        User user = currentUser(auth);
        Transaction txn = pointsService.claimPoints(user, req);
        return ResponseEntity.ok(txn);
    }

    // CHANGED: delegate to PointsService and preserve original 400 behavior
    @PostMapping("/redeem")
    public ResponseEntity<Redemption> redeem(@RequestBody RedeemRequest req, Authentication auth){
        User user = currentUser(auth);
        return pointsService.redeemOffer(user, req)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> transactions(Authentication auth) {
        Long userId = currentUser(auth).getId();
        return ResponseEntity.ok(
                transactionRepository.findByUserIdOrderByDateDesc(userId)
        );
    }

    @GetMapping("/redemptions")
    public ResponseEntity<List<Redemption>> redemptions(Authentication auth){
        Long userId = currentUser(auth).getId();
        return ResponseEntity.ok(
                redemptionRepository.findByUserIdOrderByDateDesc(userId)
        );
    }
}
