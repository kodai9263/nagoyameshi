package com.example.nagoyameshi.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.ReservationRegisterForm;
import com.example.nagoyameshi.repository.ReservationRepository;

@Service
public class ReservationService {
	private final ReservationRepository reservationRepository;
	
	public ReservationService(ReservationRepository reservationRepository) {
		this.reservationRepository = reservationRepository;
	}

	// 指定したidを持つ予約を取得する。
	public Optional<Reservation> findReservationById(Integer id) {
		return reservationRepository.findById(id);
	}
	
	// 指定されたユーザーに紐づく予約を予約日時が新しい順（未来→過去）に並べ替え、ページングされた状態で取得する。
	public Page<Reservation> findReservationsByUserOrderByReservedDatetimeDesc(User user, Pageable pageable) {
		return reservationRepository.findByUserOrderByReservedDatetimeDesc(user, pageable);
	}
	
	// 予約のレコード数を取得する。
	public long countReservations() {
		return reservationRepository.count();
	}
	
	// idが最も大きい予約を取得する。
	public Reservation findFirstReservationByOrderByIdDesc() {
		return reservationRepository.findFirstByOrderByIdDesc();
	}
	
	// フォームから送信された予約情報をデータベースに登録する。
	public void createReservation(ReservationRegisterForm reservationRegisterForm, Restaurant restaurant, User user) {
		Reservation reservation = new Reservation();
		
		// LocalDateとLocalTimeをLocalDateTimeに変換
		LocalDateTime reservedDateTime = LocalDateTime.of(reservationRegisterForm.getReservationDate(), reservationRegisterForm.getReservationTime());
		
		reservation.setReservedDatetime(reservedDateTime);
		reservation.setNumberOfPeople(reservationRegisterForm.getNumberOfPeople());
		reservation.setRestaurant(restaurant);
		reservation.setUser(user);
		
		reservationRepository.save(reservation);
	}
	
	// 指定した予約をデータベースから削除する。
	public void deleteReservation(Reservation reservation) {
		reservationRepository.delete(reservation);
	}
	
	// TODO 模範回答と比べて違ったが、模範回答のコードの内容が理解できなかった
	
	// 予約日時が現在よりも2時間以上後であればtrueを返す。
	public boolean isAtLeastTwoHoursInFuture(LocalDateTime localDateTime) {
		
		LocalDateTime now = LocalDateTime.now();
		// 時間の差分を求める
		Duration duration = Duration.between(now, localDateTime);
		
		if (duration.toHours() >= 2) {
			return true;
		} else {
			return false;
		}
	}
}
