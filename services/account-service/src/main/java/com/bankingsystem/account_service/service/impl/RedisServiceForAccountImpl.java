package com.bankingsystem.account_service.service.impl;

import com.bankingsystem.account_service.dto.AccountResponseDTO;
import com.bankingsystem.account_service.service.RedisServiceForAccount;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisServiceForAccountImpl implements RedisServiceForAccount {
   @Autowired
   private RedisTemplate<String, Object> redisTemplate;

   public void saveAccount(String accountId, AccountResponseDTO accountResponseDTO) {
      this.redisTemplate.opsForValue().set(accountId, accountResponseDTO);
      this.redisTemplate.expire(accountId, Duration.ofHours(1L));
   }

   public Object getAccount(String accountId) {
      return this.redisTemplate.opsForValue().get(accountId);
   }

   public void deleteAccount(String accountId) {
      this.redisTemplate.delete(accountId);
   }

   public boolean existsAccount(String accountId) {
      return this.redisTemplate.hasKey(accountId);
   }

   public void updateAccount(String accountId, String accountData) {
      if (this.existsAccount(accountId)) {
         this.redisTemplate.opsForValue().set(accountId, accountData);
      } else {
         throw new RuntimeException("Account not found");
      }
   }
}
