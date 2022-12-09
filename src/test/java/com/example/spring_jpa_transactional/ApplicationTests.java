package com.example.spring_jpa_transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.spring_jpa_transactional.stock.Stock;
import com.example.spring_jpa_transactional.stock.StockRepository;
import com.example.spring_jpa_transactional.stock.StockService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationTests {

  @Autowired
  StockService stockService;
  @Autowired
  StockRepository stockRepository;

  @BeforeEach
  void before() {
    // (1) 재고 객체 생성 ( 아이디가 1이고, 수량이 100개 )
    Stock stock = new Stock(1L, 100L);

    // (2) 재고 테이블에 저장
    // saveAndFlush()는 디비에 즉시 반영하는 함수
    stockRepository.saveAndFlush(stock);
  }

  @AfterEach
  void after() {
    // (1) 재고 테이블에 저장된 모든 데이터 삭제
    stockRepository.deleteAll();
  }

  @Test
  void decrease() { // TEST SUCCESS
    // (1) 아이디가 1인 재고 데이터의 수량을 1만큼 감소
    stockService.decrease(1L, 1L);

    // (2) 아이디가 1인 재고 데이터 조회
    Stock stock = stockRepository.findById(1L).orElseThrow();

    // (3) 조회된 재고의 수량이 99인지 비교
    assertEquals(stock.getQuantity(), 99L);
  }

  @Test
  void 동시에_100개의_요청() throws InterruptedException { // TEST FAIL
    // (1) 스레드 개수를 100개로 지정
    int threadCount = 100;

    // (2) ExecutorService 병렬 작업 시 여러 개의 작업을 효율적으로 처리하기 위해 제공되는 JAVA 라이브러리이다.
    // 32개의 스레드를 생성해서 활성화.
    ExecutorService executorService = Executors.newFixedThreadPool(32);

    // (3) CountDownLatch 어떤 쓰레드가 다른 쓰레드에서 작업이 완료될 때 까지 기다릴 수 있도록 해주는 클래스이다.
    CountDownLatch latch = new CountDownLatch(threadCount);

    // (4) 100개의 스레드에 대해, 아이디가 1인 재고의 수량을 1씩 감소
    for (int i = 0; i < threadCount; i++) {
      executorService.submit(() -> {
        try {
          stockService.decrease(1L, 1L);
        } finally {
          latch.countDown();;
        }
      });
    }

    // (5) 다른 스레드에서 수행 중인 작업이 완료 될 때까지 대기
    latch.await();

    // (6) 아이디가 1인 재고 조회
    Stock stock = stockRepository.findById(1L).orElseThrow();

    // (7) race condition 이 발생한다. - 이는 동시에 변경하려고 할때 발생하는 문제이다.
    // 하나의 쓰레드의 작업이 완료되기 이전에 쓰레드가 공유 자원에 접근하였기 때문에 값이 공유 자원의 값이 다르다.
    assertEquals(0L, stock.getQuantity());

  }

  @Test
  void PessimisticLock_동시에_100개의_요청() throws InterruptedException { // TEST SUCCESS
    int threadCount = 100;

    ExecutorService executorService = Executors.newFixedThreadPool(32);

    CountDownLatch latch = new CountDownLatch(threadCount);

    for (int i = 0; i < threadCount; i++) {
      executorService.submit(() -> {
        try {
          stockService.pessimisticLockDecrease(1L, 1L); // 이 부분의 함수가 달라짐.
        } finally {
          latch.countDown();;
        }
      });
    }

    latch.await();
    Stock stock = stockRepository.findById(1L).orElseThrow();

    assertEquals(0L, stock.getQuantity());
  }

  @Test
  void PessimisticLock_READ_동시에_100개의_요청() throws InterruptedException { // TEST FAIL
    int threadCount = 100;

    ExecutorService executorService = Executors.newFixedThreadPool(32);

    CountDownLatch latch = new CountDownLatch(threadCount);

    for (int i = 0; i < threadCount; i++) {
      executorService.submit(() -> {
        try {
          stockService.pessimisticLockReadDecrease(1L, 1L); // 이 부분의 함수가 달라짐.
        } finally {
          latch.countDown();;
        }
      });
    }

    latch.await();
    Stock stock = stockRepository.findById(1L).orElseThrow();

    assertEquals(0L, stock.getQuantity());
  }

  @Test
  void OptimisticLock_동시에_100개의_요청() throws InterruptedException { // TEST FAIL
    int threadCount = 100;

    ExecutorService executorService = Executors.newFixedThreadPool(32);

    CountDownLatch latch = new CountDownLatch(threadCount);

    for (int i = 0; i < threadCount; i++) {
      executorService.submit(() -> {
        try {
          stockService.optimisticLockDecrease(1L, 1L); // 이 부분의 함수가 달라짐.
        } finally {
          latch.countDown();;
        }
      });
    }

    latch.await();
    Stock stock = stockRepository.findById(1L).orElseThrow();

    assertEquals(0L, stock.getQuantity());
  }
}
