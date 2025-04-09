package com.example.nagoyameshi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Restaurant;

public interface RestaurantRepository extends JpaRepository<Restaurant, Integer> {
	public Page<Restaurant> findByNameLike(String nameKeyword, Pageable pageable);
	public Restaurant findFirstByOrderByIdDesc();
	
	// すべての店舗を作成日時が新しい順に並べ替え、ページングされた状態で取得する
	public Page<Restaurant> findAllByOrderByCreatedAtDesc(Pageable pageable);
	
	// すべての店舗を最低価格が安い順に並べ替え、ページングされた状態で取得する
	public Page<Restaurant> findAllByOrderByLowestPriceAsc(Pageable pageable);
	
	// 指定されたキーワードを店舗名または住所またはカテゴリ名に含む店舗を作成日時が新しい順に並べ替え、ページングされた状態で取得する
	@Query("SELECT DISTINCT r FROM Restaurant r " +
		   "LEFT JOIN r.categoriesRestaurants cr " +
		   "LEFT JOIN cr.category c " +
		   "WHERE r.name LIKE :nameKeyword " +
		   "OR r.address LIKE :addressKeyword " +
		   "OR c = :category " +
		   "ORDER BY r.createdAt DESC")
	public Page<Restaurant> findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByCreatedAtDesc(
		@Param("nameKeyword") String nameKeyword, 
		@Param("addressKeyword") String addressKeyword, 
		@Param("category") Category category, 
		Pageable pageable);

	// 指定されたキーワードを店舗名または住所またはカテゴリ名に含む店舗を最低価格が安い順に並べ替え、ページングされた状態で取得する。
	@Query("SELECT DISTINCT r FROM Restaurant r " +
		   "LEFT JOIN r.categoriesRestaurants cr " +
		   "LEFT JOIN cr.category c " +
		   "WHERE r.name LIKE :nameKeyword " +
		   "OR r.address LIKE :addressKeyword " +
		   "OR c = :category " +
		   "ORDER BY r.lowestPrice ASC")
	public Page<Restaurant> findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByLowestPriceAsc(
		@Param("nameKeyword") String nameKeyword, 
		@Param("addressKeyword") String addressKeyword, 
		@Param("category") Category category, 
		Pageable pageable);
	
	// 指定されたidのカテゴリが設定された店舗を作成日時が新しい順に並べ替え、ページングされた状態で取得する。
	@Query("SELECT DISTINCT r FROM Restaurant r " +
		   "JOIN r.categoriesRestaurants cr " +
		   "JOIN cr.category c " +
		   "WHERE c = :category " +
		   "ORDER BY r.createdAt DESC")
	public Page<Restaurant> findByCategoryIdOrderByCreatedAtDesc(
		@Param("category") Category category, 
		Pageable pageable);
	
	// 指定されたidのカテゴリが設定された店舗を最低価格が安い順に並べ替え、ページングされた状態で取得する。
	@Query("SELECT DISTINCT r FROM Restaurant r " +
		   "JOIN r.categoriesRestaurants cr " +
		   "JOIN cr.category c " +
		   "WHERE c = :category " +
		   "ORDER BY r.lowestPrice ASC")
	public Page<Restaurant> findByCategoryIdOrderByLowestPriceAsc(
		@Param("category") Category category, 
		Pageable pageable);
	
	// 指定された最低価格以下の店舗を作成日時が新しい順に並べ替え、ページングされた状態で取得する。
	public Page<Restaurant> findByLowestPriceLessThanEqualOrderByCreatedAtDesc(Integer lowestPrice, Pageable pageable);
	
	// 指定された最低価格以下の店舗を最低価格が安い順に並べ替え、ページングされた状態で取得する。
	public Page<Restaurant> findByLowestPriceLessThanEqualOrderByLowestPriceAsc(Integer lowestPrice, Pageable pageable);
	
	// TODO クエリメソッド
	// すべての店舗を平均評価が高い順に並べ替え、ページングされた状態で取得する。
	@Query("SELECT r FROM Restaurant r " +
		   "LEFT JOIN r.reviews re " +
		   "GROUP BY r.id " +
		   "ORDER BY AVG(re.score) DESC")
	Page<Restaurant> findAllByOrderByAverageScoreDesc(Pageable pageable);
	
	// 指定されたキーワードを店舗名または住所またはカテゴリ名に含む店舗を平均評価が高い順に並べ替え、ページングされた状態で取得する。
	@Query("SELECT DISTINCT r FROM Restaurant r " + 
		   "LEFT JOIN r.categoriesRestaurants cr " + 
		   "LEFT JOIN cr.category c " + 
		   "LEFT JOIN r.reviews re " + 
		   "WHERE r.name LIKE :nameKeyword " + 
		   "OR r.address LIKE :addressKeyword " + 
		   "OR c = :category " + 
		   "GROUP BY r.id " + 
		   "ORDER BY AVG(re.score) DESC")
	Page<Restaurant> findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByAverageScoreDesc(
			@Param("nameKeyword") String nameKeyword,
			@Param("addressKeyword") String addressKeyword,
			@Param("category") Category category,
			Pageable pageable);
	
	// 指定されたidのカテゴリが設定された店舗を平均評価が高い順に並べ替え、ページングされた状態で取得する。
	@Query("SELECT DISTINCT r FROM Restaurant r " + 
		   "LEFT JOIN r.categoriesRestaurants cr " + 
		   "LEFT JOIN cr.category c " + 
		   "LEFT JOIN r.reviews re " + 
		   "WHERE c = :category " + 
		   "GROUP BY r.id " + 
		   "ORDER BY AVG(re.score) DESC")
	Page<Restaurant> findByCategoryIdOrderByAverageScoreDesc(
			@Param("category") Category category,
			Pageable pageable);
	
	// 指定された最低価格以下の店舗を平均評価が高い順に並べ替え、ページングされた状態で取得する。
	@Query("SELECT DISTINCT r FROM Restaurant r " + 
		   "LEFT JOIN r.reviews re " + 
		   "GROUP BY r.id " + 
		   "ORDER BY AVG(re.score) DESC")
	Page<Restaurant> findByLowestPriceLessThanEqualOrderByAverageScoreDesc(Integer lowestPrice, Pageable pageable);
}
