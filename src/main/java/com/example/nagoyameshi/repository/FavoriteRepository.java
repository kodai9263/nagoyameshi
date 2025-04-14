package com.example.nagoyameshi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.nagoyameshi.entity.Favorite;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.User;

public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {

	// 指定した店舗とユーザーが紐づいたお気に入りを取得するメソッド
	public Favorite findByRestaurantAndUser(Restaurant restaurant, User user);
	
	// 指定したユーザーのすべてのお気に入りを作成日時が新しい順に並べ替え、ページングされた状態で取得するメソッド
	@Query("SELECT f FROM Favorite f WHERE f.user = :user ORDER BY f.createdAt DESC")
	public Page<Favorite> findAllUserOrderByCreatedAtDesc(User user, Pageable pageable);
}
