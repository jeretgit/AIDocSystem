package com.aidoc.repository;

import com.aidoc.entity.DocumentInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public interface DocumentInfoRepository extends JpaRepository<DocumentInfo, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE DocumentInfo d SET d.processStatus = :status, d.globalSummary = :summary, d.updateTime = :updateTime WHERE d.id = :id")
    void updateStatusAndSummary(@Param("id") Long id, @Param("status") Integer status, @Param("summary") String summary, @Param("updateTime") LocalDateTime updateTime);
}
