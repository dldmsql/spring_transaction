package com.example.spring_jpa_transactional.stock;

import java.util.Optional;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockRepository extends JpaRepository<Stock, Long> {

  Optional<Stock> findById(Long id);

  @Lock(value = LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from Stock s where s.id = :id")
  Stock findByWithPessimisticLock(@Param("id") Long id);

  @Lock(value = LockModeType.PESSIMISTIC_READ)
  @Query("select s from Stock s where s.id = :id")
  Stock findByWithPessimisticLockRead(@Param("id") Long id);

  @Lock(value = LockModeType.OPTIMISTIC)
  @Query("select s from Stock s where s.id = :id")
  Stock findByWithOptimisticLock(@Param("id") Long id);
}
