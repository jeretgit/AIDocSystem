package com.aidoc.repository;

import com.aidoc.entity.DocumentInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentInfoRepository extends JpaRepository<DocumentInfo, Long> {
}
