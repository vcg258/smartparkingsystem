package com.example.parkingsystem.service.auth;


import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Log4j2
public enum MailService {
    INSTANCE;

    private String host;
    private String port;
    private String user;
    private String pass;

    // 구동시 기본 값들을 가져오기.
    MailService() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("mail.properties")) {
            Properties prop = new Properties();

            if (input == null) {
                return;
            } // 못찾은 경우 해당 내용을 종료

            // 데이터가 있으면 로딩해서 값을 가져오기
            prop.load(input);

            this.host = prop.getProperty("mail.host");
            this.port = prop.getProperty("mail.port");
            this.user = prop.getProperty("mail.userName");
            this.pass = prop.getProperty("mail.password");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // OTP 발송
    public boolean sendAuthEmail(String to, String otpCode) {
        boolean result = false;

        Properties props = new Properties();

        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        log.info("AuthEmail : 호스트 {}, 포트 {}, 유저 {}, 패스 {}", host, port, user, pass);

        // 로드된 user와 pass를 사용해 인증 세션 생성
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });


        String subject = "[스마트 주차시스템] OTP 인증 번호";
        String content =
                "<div style='font-family:Arial, sans-serif; max-width:600px; margin:0 auto;'>" +

                        "<h2 style='color:#2c3e50;'>스마트 주차관리 시스템</h2>" +

                        "<p>아래 OTP 인증번호를 입력하여 본인 확인을 완료해주세요.</p>" +

                        "<div style='padding:20px; background:#f4f4f4; text-align:center; " +
                        "font-size:32px; font-weight:bold; letter-spacing:8px; border-radius:8px;'>"
                        + otpCode +
                        "</div>" +

                        "<p style='font-size:13px; color:gray; margin-top:20px;'>※ 인증번호는 4분간 유효합니다.</p>" +
                        "<p style='font-size:13px; color:gray;'>※ 본인이 요청하지 않은 경우 이 메일을 무시하세요.</p>" +

                        "<hr style='margin-top:30px;'>" +
                        "<p style='font-size:12px; color:#aaa;'>© 스마트 주차관리 시스템</p>" +

                        "</div>";

        // 메일 발송을 시도.
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(content, "text/html; charset=UTF-8");


            Transport.send(message);
            result = true;
        } catch (MessagingException e) {
            throw new RuntimeException("메일 발송 실패", e);
        }
        return result;
    }

    // uuidPw 발송
    public boolean sendAuthPw(String to, String pw) {
        boolean result = false;

        Properties props = new Properties();

        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        log.info("AuthPw 호스트 {}, 포트 {}, 유저 {}, 패스 {}", host, port, user, pass);

        // 로드된 user와 pass를 사용해 인증 세션 생성
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });


        String subject = "[스마트 주차시스템] 재설정 비밀번호";
        String content =
                "<div style='font-family:Arial, sans-serif; max-width:600px; margin:0 auto;'>" +

                        "<h2 style='color:#2c3e50;'>스마트 주차관리 시스템</h2>" +

                        "<p>요청하신 임시 비밀번호가 발급되었습니다.</p>" +
                        "<p>아래 비밀번호로 로그인 후 반드시 새로운 비밀번호로 변경해주세요.</p>" +

                        "<div style='padding:20px; background:#f4f4f4; text-align:center; " +
                        "font-size:26px; font-weight:bold; letter-spacing:3px; border-radius:8px;'>"
                        + pw +
                        "</div>" +

                        "<p style='font-size:13px; color:gray; margin-top:20px;'>※ 보안을 위해 로그인 후 즉시 비밀번호를 변경해주세요.</p>" +
                        "<p style='font-size:13px; color:gray;'>※ 본인이 요청하지 않은 경우 고객센터에 문의하세요.</p>" +

                        "<hr style='margin-top:30px;'>" +
                        "<p style='font-size:12px; color:#aaa;'>© 스마트 주차관리 시스템</p>" +

                        "</div>";

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(content, "text/html; charset=UTF-8");


            Transport.send(message);
            result = true;
        } catch (MessagingException e) {
            throw new RuntimeException("메일 발송 실패", e);
        }
        return result;
    }
}
