package com.zalo.Spring_Zalo.Repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.zalo.Spring_Zalo.Entities.Customer;
import com.zalo.Spring_Zalo.Entities.CustomerReward;
import com.zalo.Spring_Zalo.Entities.Event;
import com.zalo.Spring_Zalo.Entities.Reward;

import java.util.List;
import java.util.Optional;

public interface CustomerRewardRepo extends JpaRepository<CustomerReward, Integer> {
        @Query("select c from CustomerReward c  where c.customer.id =:customerId")
        List<CustomerReward> findByCustomer_Id(@Param("customerId") Integer customerId);

        @Query("select c from CustomerReward c  where c.customer.id =:customerId and c.reward.id =:rewarId")
        CustomerReward findByCustomerIdAndReWardId(@Param("customerId") Integer customerId,
                        @Param("rewarId") Integer rewarId);

        @Query("select c.reward,c.customer.phone,c.exchangeRewardDate,c.status from CustomerReward c  where c.customer.id =:customerId and c.event.id =:eventId")
        List<Object[]> findByCustomerIdAndEventId(@Param("customerId") Integer customerId,
                        @Param("eventId") Integer eventId);

        @Query("select c from CustomerReward c where c.reward.id =:rewarId")
        List<CustomerReward> findByReWardId(@Param("rewarId") Integer rewarId);

        /*
         * select customer Reward folowing status
         * 1: reward that user register to get chance to win
         * 2: reward that user already recive
         */
        @Query("select c from CustomerReward c where c.status = :status")
        List<CustomerReward> findByStatus(@Param("status") Integer status);

        /*
         * find all reward in that event ( find buy Event ID )
         */
        @Query("select c.reward,c.customer.phone,c.exchangeRewardDate,c.status from CustomerReward c where c.event.id =:eventId")
        List<Object[]> findByEventId(@Param("eventId") Integer eventId);

        @Query("select c.reward from CustomerReward c")
        List<Object> findByAll();

        @Query("select c.reward from CustomerReward c where c.customer.id =:customerId and c.status=1")
        List<Object> findByCustomerId(@Param("customerId") Integer customerId);

        @Query("select c.reward from CustomerReward c where c.event.id =:eventId")
        Page<Object> findPageEventId(@Param("eventId") Integer eventId, Pageable pageable);

}
