package com.example.nagoyameshi.form;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class ReservationRegisterForm {
	@NotNull(message = "予約日を選択してください。")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate reservationDate;
	
	@NotNull(message = "時間を選択してください。")
	@DateTimeFormat(pattern =  "HH:mm:ss")
	private LocalTime reservationTime;
	
	@NotNull(message = "座席数を入力してください。")
	@Min(value = 0, message = "座席数は0席以上に設定してください。")
	private Integer numberOfPeople;

}
