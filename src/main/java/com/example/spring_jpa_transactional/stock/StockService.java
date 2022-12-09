package com.example.spring_jpa_transactional.stock;

import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockService {

  private final StockRepository stockRepository;

  @Transactional
  public void decrease(Long id, Long quantity) {
    // (1) 재고 테이블에서 아이디를 기준으로 조회
    Stock stock = stockRepository.findById(id).orElseThrow();

    // (2) 조회된 재고의 수량 줄이기
    stock.decrease(quantity);
  }

  @Transactional
  public void pessimisticLockDecrease(Long id, Long quantity) {

    // 다른 트랜잭션이 특정 row에 작업을 하는 것을 막아버리는 행위이다.
    // 테스트 시나리오에 따라, 100개의 수량이 있는 아이디가 1인 재고 객체의 수량을 다른 트랜잭션에서 접근하여 1씩 감소한다.
    // 이때 처음 시도한 트랜잭션은 성공적으로 update 쿼리를 발생시킬 수 있지만, 다른 트랜잭션들은 pessimistic lock이 걸려 있기 때문에
    // update 쿼리의 결과가 커밋되기 까지 대기 상태이다. 이는 성능 저하를 유발한다.
    Stock stock = stockRepository.findByWithPessimisticLock(id);

    stock.decrease(quantity);
  }

  @Transactional
  public void pessimisticLockReadDecrease(Long id, Long quantity) {

    // 데이터를 반복 읽기만 하고 수정하지 않는 용도로 lock을 걸 때 사용한다. 일반적으로는 잘 사용하지 않는다.
    // Shared lock은 다른 사용자가 동시에 데이터를 읽을 수는 있지만 쓰기는 할 수 없다.
    // Shared lock을 얻고 데이터가 업데이트되거나 삭제되지 않도록 한다.
    Stock stock = stockRepository.findByWithPessimisticLockRead(id);

    stock.decrease(quantity);
  }

  @Transactional
  public void optimisticLockDecrease(Long id, Long quantity) {

    Stock stock = stockRepository.findByWithOptimisticLock(id);

    stock.decrease(quantity);
  }

}