package com.example.nagoyameshi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.entity.User;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
	
	//　指定した店舗とユーザーが紐づいたレビューを取得するメソッド
	public Review findByRestaurantAndUser(Restaurant restaurant, User user);
	
	// 指定した店舗のすべてのレビューを作成日時が新しい順に並べ替え、ページングされた状態で取得するメソッド
	@Query("SELECT r FROM Review r WHERE r.restaurant = :restaurant ORDER BY r.createdAt DESC")
	public Page<Review> findAllByRestaurantOrderByCreatedAtDesc(Restaurant restaurant, Pageable pageable);
	
	// idが最も大きいレビューを取得する（idを基準に降順で並べ替え、最初の1件を取得する）メソッド
	public Review findFirstByOrderByIdDesc();
}
