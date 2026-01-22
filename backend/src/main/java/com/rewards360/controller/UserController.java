package com.rewards360.controller;

import java.time.LocalDate;
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

    @PostMapping("/claim")
    public ResponseEntity<Transaction> claim(@RequestBody ClaimRequest req, Authentication auth){
        User user = currentUser(auth);
        Transaction txn = Transaction.builder()
                .externalId("CLAIM-"+System.currentTimeMillis())
                .type("CLAIM")
                .pointsEarned(req.points())
                .pointsRedeemed(0)
                .store("Online")
                .date(LocalDate.now())
                .expiry(LocalDate.now().plusMonths(3))
                .note(req.note())
                .user(user)
                .build();
        user.getProfile().setPointsBalance(user.getProfile().getPointsBalance()+req.points());
        transactionRepository.save(txn);
        userRepository.save(user);
        return ResponseEntity.ok(txn);
    }

    @PostMapping("/redeem")
    public ResponseEntity<Redemption> redeem(@RequestBody RedeemRequest req, Authentication auth){
        User user = currentUser(auth);
        Offer offer = offerRepository.findById(req.offerId()).orElseThrow();
        if(user.getProfile().getPointsBalance() < offer.getCostPoints()){
            return ResponseEntity.badRequest().build();
        }
        user.getProfile().setPointsBalance(user.getProfile().getPointsBalance()-offer.getCostPoints());
        Transaction txn = Transaction.builder()
                .externalId("RED-"+System.currentTimeMillis())
                .type("REDEMPTION")
                .pointsEarned(0)
                .pointsRedeemed(offer.getCostPoints())
                .store(req.store())
                .date(LocalDate.now())
                .note(offer.getTitle())
                .user(user)
                .build();
        transactionRepository.save(txn);
        Redemption red = Redemption.builder()
                .transactionId(txn.getExternalId())
                .confirmationCode("CONF-"+System.currentTimeMillis())
                .date(LocalDate.now())
                .costPoints(offer.getCostPoints())
                .offerTitle(offer.getTitle())
                .store(req.store())
                .user(user)
                .build();
        redemptionRepository.save(red);
        userRepository.save(user);
        return ResponseEntity.ok(red);
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> transactions(Authentication auth){
        return ResponseEntity.ok(transactionRepository.findTop10ByUserOrderByDateDesc(currentUser(auth)));
    }

    @GetMapping("/redemptions")
    public ResponseEntity<List<Redemption>> redemptions(Authentication auth){
        return ResponseEntity.ok(redemptionRepository.findByUser(currentUser(auth)));
    }
}