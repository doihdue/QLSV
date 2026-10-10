package com.demo.be.repository;

import com.demo.be.model.DotRenLuyen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DotRenLuyenRepository extends JpaRepository<DotRenLuyen, Long> {
    Optional<DotRenLuyen> findFirstByDangMoTrueOrderByIdDesc();
    List<DotRenLuyen> findAllByOrderByIdDesc();
}
