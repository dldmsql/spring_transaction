package com.example.spring_jpa_transactional.stock;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Stock {
  @Id
  @GeneratedValue
  @Column(name = "stock_id")
  private Long id;

  private Long productId;

  //optimistic lock 에서 사용
  @Version
  private Long quantity;

  public Stock(Long productId, Long quantity) {
    this.productId = productId;
    this.quantity = quantity;
  }

  public void decrease(Long quantity) {
    if (this.quantity - quantity < 0) { // (1) 현재 수량에서 파라미터로 넘겨 받은 양만큼 감소시킨 값이 0보다 작다면, 에러
      throw new RuntimeException("foo");
    }

    this.quantity = this.quantity - quantity;
  }

}
