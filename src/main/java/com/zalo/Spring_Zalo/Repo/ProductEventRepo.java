package com.zalo.Spring_Zalo.Repo;

import org.antlr.v4.runtime.atn.SemanticContext.AND;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.zalo.Spring_Zalo.Entities.ProductEvent;

import java.util.List;
import java.util.Optional;

public interface ProductEventRepo extends JpaRepository<ProductEvent, Integer> {
        @Query("select p from ProductEvent p where p.event.id = :eventId")
        Optional<ProductEvent> findByProduct(@Param("eventId") Integer eventId);

        @Query("select p from ProductEvent p where p.event.id = :eventId")
        List<ProductEvent> findAllByEventId(@Param("eventId") Integer eventId);

        /*
         * this query use to check if the product exist(follow eventId ) and get product
         * point
         */
        @Query("SELECT pe.product.point FROM ProductEvent pe " +
                        "JOIN pe.product p " +
                        "JOIN pe.event e " +
                        "WHERE p.name = :productName AND e.id = :eventId")
        Optional<Integer> getPointByProductName(@Param("productName") String productName,
                        @Param("eventId") Integer eventId);

        @Query("SELECT pe FROM ProductEvent pe JOIN pe.event e WHERE pe.product.id = :productId")
        Optional<ProductEvent> findByProductId(@Param("productId") Integer productId);
}
