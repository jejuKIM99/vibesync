package com.util;

import java.sql.*;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordMigrator {

	/**
     * 평문 비밀번호와 Salt를 받아 SHA-256 해시 값을 생성하는 메소드
     * @param plainTextPassword 사용자로부터 입력받은 평문 비밀번호
     * @param base64Salt        DB에 저장된 해당 유저의 Base64 인코딩된 Salt 값
     * @return                  해싱된 비밀번호 (16진수 문자열)
     */
    public static String hashPassword(String plainTextPassword, String base64Salt) {
        // [SHA-256 해싱 로직]
        // Null 또는 빈 문자열일 경우 예외 처리 또는 특정 값 반환
    	/*
        if (plainTextPassword == null || plainTextPassword.isEmpty() || base64Salt == null || base64Salt.isEmpty()) {
            // 적절한 오류 처리 또는 빈 문자열 반환 등
        }
        */
        	if (plainTextPassword == null || plainTextPassword.isEmpty()) {
                throw new IllegalArgumentException("비밀번호는 null이거나 비어있을 수 없습니다.");
            }
            if (base64Salt == null || base64Salt.isEmpty()) {
                throw new IllegalArgumentException("Salt는 null이거나 비어있을 수 없습니다.");
            }
            
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] saltBytes = Base64.getDecoder().decode(base64Salt);
            md.update(saltBytes);
            byte[] hashedBytes = md.digest(plainTextPassword.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // 이 예외는 일반적으로 발생하기 어렵지만, 발생하면 심각한 시스템 설정 문제입니다.
            throw new PasswordHashingException("비밀번호 해싱 중 심각한 오류 발생 (알고리즘 없음)", e);
        } catch (UnsupportedEncodingException e) {
            // 이 예외 역시 일반적으로 발생하기 어렵습니다.
            throw new PasswordHashingException("비밀번호 해싱 중 심각한 오류 발생 (인코딩 지원 안함)", e);
        } catch (IllegalArgumentException e) { // Base64 디코딩 실패 시
            throw new PasswordHashingException("잘못된 Salt 형식으로 인해 비밀번호 해싱에 실패했습니다.", e);
        	}
        }

    public static String generateSalt() throws Exception {
        SecureRandom sr = SecureRandom.getInstanceStrong();
        byte[] saltBytes = new byte[16];
        sr.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }

}
