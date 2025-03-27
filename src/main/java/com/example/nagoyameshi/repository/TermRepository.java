package com.example.nagoyameshi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.Term;

public interface TermRepository extends JpaRepository<Term, Integer>{
	// idが最も大きい利用規約を取得する
	public Term findFirstByOrderByIdDesc();

}
