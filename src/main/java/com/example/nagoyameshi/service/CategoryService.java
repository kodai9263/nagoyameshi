package com.example.nagoyameshi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.form.CategoryEditForm;
import com.example.nagoyameshi.form.CategoryRegisterForm;
import com.example.nagoyameshi.repository.CategoryRepository;

@Service
public class CategoryService {
	private final CategoryRepository categoryRepository;
	
	public CategoryService(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}
	
	// すべてのカテゴリをリスト形式で取得する
	public List<Category> findAllCategories() {
		return categoryRepository.findAll();
	}
	// すべてのカテゴリをページングされた状態で取得する。
	public Page<Category> findAllCategories(Pageable pageable) {
		return categoryRepository.findAll(pageable);
	}
	
	// 指定したカテゴリ名を持つ最初のカテゴリを取得する。
	public Category findFirstCategoryByName(String name) {
		return categoryRepository.findFirstByName(name);
	}
	
	// 指定されたキーワードをカテゴリ名に含むカテゴリを、ページングされた状態で取得する。
	public Page<Category> findCategoriesByNameLike(String nameKeyword, Pageable pageable) {
		return categoryRepository.findByNameLike("%" + nameKeyword + "%", pageable);
	}
	
	// 指定したidを持つカテゴリを取得する。
	public Optional<Category> findCategoryById(Integer id) {
		if (id == null) {
			return Optional.empty();
		}
		return categoryRepository.findById(id);
	}
	
	// カテゴリのレコード数を取得する。
	public long countCategories() {
		return categoryRepository.count();
	}

	// idが最も大きいカテゴリを取得する。
	public Category findFirstCategoryByOrderByIdDesc() {
		return categoryRepository.findFirstByOrderByIdDesc();
	}
	
	// フォームから送信されたカテゴリ情報をデータベースに登録する。
	@Transactional
	public void createCategory(CategoryRegisterForm categoryRegisterForm) {
		Category category = new Category();
		
		category.setName(categoryRegisterForm.getName());
		
		categoryRepository.save(category);
	}
	
	// フォームから送信されたカテゴリ情報でデータベースを更新する。
	@Transactional
	public void updateCategoy(CategoryEditForm categoryEditForm, Category category) {
		category.setName(categoryEditForm.getName());
		
		categoryRepository.save(category);
	}
	
	// 指定したカテゴリをデータベースから削除する。
	@Transactional
	public void deleteCategory(Category category) {
		categoryRepository.delete(category);
	}
}
