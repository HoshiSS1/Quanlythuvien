package com.example.demo5.Service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service gửi email thông báo quá hạn cho bạn đọc.
 * Sử dụng Jakarta Mail + Gmail SMTP.
 * Hỗ trợ: gửi đơn lẻ, gửi nhóm (group theo bạn đọc), tracking pixel, HTML mobile-friendly.
 */
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final DateTimeFormatter VN_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Path CONFIG_PATH = Paths.get(System.getProperty("user.home"), ".lms_email.properties");

    private String smtpHost;
    private int smtpPort;
    private String fromEmail;
    private String appPassword;

    public EmailService() {
        loadConfig();
    }

    // ========================== CẤU HÌNH ==========================

    private void loadConfig() {
        Properties props = new Properties();
        if (Files.exists(CONFIG_PATH)) {
            try (InputStream is = Files.newInputStream(CONFIG_PATH)) {
                props.load(is);
            } catch (IOException e) {
                logger.warn("Không đọc được file cấu hình: {}", e.getMessage());
            }
        } else {
            try (InputStream is = getClass().getResourceAsStream("/email.properties")) {
                if (is != null) props.load(is);
            } catch (IOException e) {
                logger.warn("Không tìm thấy email.properties trong resources");
            }
        }
        this.smtpHost = props.getProperty("mail.smtp.host", "smtp.gmail.com");
        this.smtpPort = Integer.parseInt(props.getProperty("mail.smtp.port", "587"));
        this.fromEmail = props.getProperty("mail.from", "");
        this.appPassword = props.getProperty("mail.password", "");
    }

    public void saveConfig(String host, int port, String email, String password) {
        this.smtpHost = host;
        this.smtpPort = port;
        this.fromEmail = email;
        this.appPassword = password;

        Properties props = new Properties();
        props.setProperty("mail.smtp.host", host);
        props.setProperty("mail.smtp.port", String.valueOf(port));
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.starttls.enable", "true");
        props.setProperty("mail.from", email);
        props.setProperty("mail.password", password);

        try (OutputStream os = Files.newOutputStream(CONFIG_PATH)) {
            props.store(os, "LMS Prestige - Email Configuration");
            logger.info("✅ Đã lưu cấu hình email tại: {}", CONFIG_PATH);
        } catch (IOException e) {
            logger.error("❌ Không thể lưu cấu hình email: {}", e.getMessage());
        }
    }

    public boolean isConfigured() {
        return fromEmail != null && !fromEmail.isBlank()
                && appPassword != null && !appPassword.isBlank();
    }

    public String getFromEmail()  { return fromEmail; }
    public String getAppPassword() { return appPassword; }
    public String getSmtpHost()   { return smtpHost; }
    public int    getSmtpPort()   { return smtpPort; }

    // ========================== GỬI EMAIL ==========================

    /**
     * Tạo SMTP Session dùng chung.
     */
    private Session createSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", String.valueOf(smtpPort));
        props.put("mail.smtp.auth", "true");
        // Giảm cực độ timeout để "thanh gửi không bị lag" nếu nhập sai port
        props.put("mail.smtp.connectiontimeout", "4000"); 
        props.put("mail.smtp.timeout", "5000");       

        if (smtpPort == 465) {
            // Cấu hình bắt buộc cho Port 465 (SSL)
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        } else {
            // Cấu hình bắt buộc cho Port 587 (STARTTLS)
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        }

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, appPassword);
            }
        });
    }

    /**
     * Gửi email thông báo quá hạn cho 1 bạn đọc (có thể nhiều phiếu).
     * @param toEmail       email bạn đọc
     * @param readerName    tên bạn đọc
     * @param loanDetails   danh sách phiếu quá hạn [{loanId, bookTitle, borrowDate, returnDate, overdueDays}]
     * @return "OK" nếu thành công, ngược lại trả về chuỗi báo lỗi chi tiết
     */
    public String sendGroupedOverdueNotification(String toEmail, String readerName,
                                                   List<Map<String, String>> loanDetails) {
        if (!isConfigured()) {
            return "Chưa cấu hình tài khoản SMTP!";
        }

        try {
            Session session = createSession();
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail, "LMS Prestige - Thư Viện", "UTF-8"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            
            // BCC (Gửi ẩn) 1 bản copy trực tiếp về hộp thư Inbox của chính Admin (giúp dễ dàng kiểm tra)
            message.addRecipients(Message.RecipientType.BCC, InternetAddress.parse(fromEmail));

            message.setSubject("📢 [LMS Prestige] Thông báo: Phiếu mượn sách của bạn đã QUÁ HẠN");
            message.setContent(buildGroupedEmailContent(readerName, toEmail, loanDetails),
                    "text/html; charset=UTF-8");
            message.setSentDate(new java.util.Date());
            message.setHeader("X-Mailer", "LMS Prestige Notification System");
            message.setHeader("X-Priority", "3");

            Transport.send(message);
            logger.info("══════════════════════════════════════════════");
            logger.info("✅ EMAIL GỬI THÀNH CÔNG");
            logger.info("   Đến: {} ({})", readerName, toEmail);
            for (Map<String, String> loan : loanDetails) {
                logger.info("   📖 {} | Quá hạn {} ngày", loan.get("bookTitle"), loan.get("overdueDays"));
            }
            logger.info("══════════════════════════════════════════════");
            return "OK";
        } catch (AuthenticationFailedException e) {
            logger.error("❌ Mật khẩu Ứng Dụng (App Password) sai hoặc tài khoản bảo mật 2 lớp chưa bật.");
            return "Sai mật khẩu ứng dụng Gmail (Authentication failed)";
        } catch (MessagingException e) {
            logger.error("❌ Lỗi cấu hình mạng / kết nối SMTP: {}", e.getMessage());
            return "Lỗi cấu hình Port/Host hoặc mất kết nối: " + e.getMessage();
        } catch (Exception e) {
            logger.error("❌ Lỗi không xác định: {}", e.getMessage());
            return "Lỗi cấu trúc Mail: " + e.getMessage();
        }
    }

    /**
     * Gửi email hàng loạt, nhóm theo bạn đọc (không gửi trùng). Cập nhật tiến độ qua callback.
     */
    public Map<String, Object> sendBatchOverdueNotifications(List<Map<String, String>> overdueList, 
                                                             java.util.function.BiConsumer<Integer, Integer> progressCallback) {
        Map<String, Object> result = new HashMap<>();
        List<String> details = new ArrayList<>();
        int successReaders = 0, failReaders = 0;
        int totalLoans = overdueList.size();

        if (overdueList.isEmpty()) {
            result.put("successReaders", 0);
            result.put("failReaders", 0);
            result.put("totalLoans", 0);
            result.put("totalReaders", 0);
            result.put("details", List.of("Không có phiếu quá hạn nào."));
            return result;
        }

        // Nhóm phiếu theo bạn đọc
        Map<String, List<Map<String, String>>> grouped = new LinkedHashMap<>();
        for (Map<String, String> entry : overdueList) {
            String key = entry.get("readerName") + "|" + entry.getOrDefault("email", "");
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(entry);
        }

        int currentGroup = 0;
        int totalGroups = grouped.size();

        for (Map.Entry<String, List<Map<String, String>>> group : grouped.entrySet()) {
            currentGroup++;
            // Báo lại tiến trình cho UI
            if (progressCallback != null) {
                progressCallback.accept(currentGroup, totalGroups);
            }

            try {
                // Thêm độ trễ nhỏ (700ms) để giao diện tiến trình chạy mượt và "từ từ" như người dùng muốn
                Thread.sleep(700);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            List<Map<String, String>> loans = group.getValue();
            String readerName = loans.get(0).get("readerName");
            String email = loans.get(0).get("email");

            if (email == null || email.isBlank()) {
                failReaders++;
                details.add("⚠ " + readerName + " — Trống email (bỏ qua)");
                continue;
            }

            String sendResultStr = sendGroupedOverdueNotification(email, readerName, loans);
            if ("OK".equals(sendResultStr)) {
                successReaders++;
                details.add("✅ " + readerName + " (" + email + ") — Gửi thành công");
            } else {
                failReaders++;
                details.add("❌ " + readerName + " (" + email + ") — " + sendResultStr);
            }
        }

        result.put("successReaders", successReaders);
        result.put("failReaders", failReaders);
        result.put("totalLoans", totalLoans);
        result.put("totalReaders", grouped.size());
        result.put("details", details);
        return result;
    }

    // ========================== TEMPLATE EMAIL HTML ==========================

    /**
     * Build nội dung email HTML chuyên nghiệp, mobile-friendly.
     * Hỗ trợ nhiều phiếu cùng 1 bạn đọc + tracking pixel.
     */
    private String buildGroupedEmailContent(String readerName, String toEmail,
                                             List<Map<String, String>> loans) {
        // Xây dựng bảng phiếu quá hạn
        StringBuilder loanRows = new StringBuilder();
        int idx = 0;
        for (Map<String, String> loan : loans) {
            idx++;
            String bgColor = idx % 2 == 0 ? "#ffffff" : "#f8fafc";
            LocalDate borrowDate = LocalDate.parse(loan.get("borrowDate"));
            LocalDate returnDate = LocalDate.parse(loan.get("returnDate"));
            String overdueDays = loan.get("overdueDays");

            loanRows.append(String.format("""
                <tr style="background-color: %s;">
                  <td style="padding:10px 14px;border-bottom:1px solid #e2e8f0;color:#475569;font-size:13px;">%s</td>
                  <td style="padding:10px 14px;border-bottom:1px solid #e2e8f0;color:#1e293b;font-weight:600;font-size:13px;">%s</td>
                  <td style="padding:10px 14px;border-bottom:1px solid #e2e8f0;color:#475569;font-size:13px;text-align:center;">%s</td>
                  <td style="padding:10px 14px;border-bottom:1px solid #e2e8f0;color:#475569;font-size:13px;text-align:center;">%s</td>
                  <td style="padding:10px 14px;border-bottom:1px solid #e2e8f0;color:#dc2626;font-weight:700;font-size:14px;text-align:center;">%s ngày</td>
                </tr>
                """,
                    bgColor,
                    loan.get("loanId"),
                    loan.get("bookTitle"),
                    borrowDate.format(VN_DATE),
                    returnDate.format(VN_DATE),
                    overdueDays
            ));
        }

        // Tracking pixel (1x1 transparent PNG)
        String trackingId = UUID.randomUUID().toString().substring(0, 8);
        String trackingPixel = String.format(
                "<img src=\"https://placehold.co/1x1/transparent/transparent?text=.&t=%s\" width=\"1\" height=\"1\" alt=\"\" style=\"display:block;\" />",
                trackingId
        );

        return String.format("""
        <!DOCTYPE html>
        <html lang="vi">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <title>Thông Báo Quá Hạn - LMS Prestige</title>
        </head>
        <body style="margin:0;padding:0;background-color:#f1f5f9;font-family:'Segoe UI','Helvetica Neue',Arial,sans-serif;-webkit-text-size-adjust:100%%;-ms-text-size-adjust:100%%;">
          
          <!-- WRAPPER -->
          <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f1f5f9;">
            <tr><td align="center" style="padding:24px 16px;">
              
              <!-- CARD -->
              <table role="presentation" width="600" cellpadding="0" cellspacing="0" style="max-width:600px;width:100%%;background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">
                
                <!-- HEADER -->
                <tr>
                  <td style="background:linear-gradient(135deg,#0d9488 0%%,#0f766e 50%%,#115e59 100%%);padding:32px 24px;text-align:center;">
                    <div style="font-size:32px;line-height:1;">📖</div>
                    <h1 style="color:#ffffff;margin:8px 0 0;font-size:20px;font-weight:700;letter-spacing:0.5px;">LMS PRESTIGE</h1>
                    <p style="color:rgba(255,255,255,0.85);margin:6px 0 0;font-size:13px;letter-spacing:1px;">THÔNG BÁO PHIẾU MƯỢN QUÁ HẠN</p>
                  </td>
                </tr>
                
                <!-- BODY -->
                <tr>
                  <td style="padding:28px 24px 20px;">
                    
                    <!-- Lời chào -->
                    <p style="font-size:15px;color:#1e293b;margin:0 0 16px;">
                      Kính gửi bạn <strong style="color:#0d9488;">%s</strong>,
                    </p>
                    
                    <p style="font-size:14px;color:#475569;line-height:1.7;margin:0 0 20px;">
                      Chúng tôi nhận thấy phiếu mượn sách của bạn đã <strong style="color:#dc2626;">quá hạn trả</strong>.
                      Dưới đây là thông tin chi tiết:
                    </p>
                    
                    <!-- BẢNG PHIẾU QUÁ HẠN -->
                    <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="border-collapse:collapse;border:1px solid #e2e8f0;border-radius:10px;overflow:hidden;margin:0 0 20px;">
                      <thead>
                        <tr style="background:linear-gradient(135deg,#f0fdfa,#ccfbf1);">
                          <th style="padding:10px 14px;text-align:left;font-size:12px;color:#0f766e;font-weight:700;border-bottom:2px solid #99f6e4;">Mã phiếu</th>
                          <th style="padding:10px 14px;text-align:left;font-size:12px;color:#0f766e;font-weight:700;border-bottom:2px solid #99f6e4;">Tên sách</th>
                          <th style="padding:10px 14px;text-align:center;font-size:12px;color:#0f766e;font-weight:700;border-bottom:2px solid #99f6e4;">Ngày mượn</th>
                          <th style="padding:10px 14px;text-align:center;font-size:12px;color:#0f766e;font-weight:700;border-bottom:2px solid #99f6e4;">Hạn trả</th>
                          <th style="padding:10px 14px;text-align:center;font-size:12px;color:#dc2626;font-weight:700;border-bottom:2px solid #99f6e4;">Quá hạn</th>
                        </tr>
                      </thead>
                      <tbody>
                        %s
                      </tbody>
                    </table>
                    
                    <!-- CTA BOX -->
                    <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="margin:0 0 20px;">
                      <tr>
                        <td style="background:#fef2f2;border-left:4px solid #ef4444;padding:14px 16px;border-radius:0 8px 8px 0;">
                          <p style="margin:0;font-size:14px;color:#991b1b;line-height:1.6;">
                            ⏰ <strong>Vui lòng mang sách đến thư viện để trả sớm nhất có thể.</strong><br>
                            Việc trả sách đúng hạn giúp đảm bảo quyền lợi mượn sách của bạn và tạo điều kiện cho các bạn đọc khác.
                          </p>
                        </td>
                      </tr>
                    </table>
                    
                    <p style="font-size:13px;color:#64748b;line-height:1.6;margin:0 0 12px;">
                      Nếu bạn đã trả sách, xin vui lòng bỏ qua email này.
                    </p>
                    
                    <!-- LIÊN HỆ -->
                    <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:#f8fafc;border-radius:8px;margin:16px 0 0;">
                      <tr>
                        <td style="padding:14px 16px;">
                          <p style="margin:0 0 6px;font-size:13px;color:#334155;font-weight:600;">📞 Liên hệ thư viện:</p>
                          <p style="margin:0;font-size:13px;color:#64748b;line-height:1.6;">
                            Email: <a href="mailto:%s" style="color:#0d9488;text-decoration:none;">%s</a><br>
                            Giờ làm việc: Thứ 2 – Thứ 6, 8:00 – 17:00
                          </p>
                        </td>
                      </tr>
                    </table>
                    
                  </td>
                </tr>
                
                <!-- SIGNATURE -->
                <tr>
                  <td style="padding:0 24px 24px;">
                    <p style="font-size:14px;color:#1e293b;margin:0;">
                      Trân trọng,<br>
                      <strong style="color:#0f766e;">Ban Quản lý Thư viện LMS Prestige</strong>
                    </p>
                  </td>
                </tr>
                
                <!-- FOOTER -->
                <tr>
                  <td style="background:#f8fafc;padding:16px 24px;text-align:center;border-top:1px solid #e2e8f0;">
                    <p style="margin:0;color:#94a3b8;font-size:11px;line-height:1.5;">
                      Email tự động từ hệ thống LMS Prestige — Vui lòng không trả lời email này.<br>
                      © 2026 LMS Prestige. All rights reserved.
                    </p>
                    %s
                  </td>
                </tr>
                
              </table>
              
            </td></tr>
          </table>
        </body>
        </html>
        """,
                readerName,
                loanRows.toString(),
                fromEmail, fromEmail,
                trackingPixel
        );
    }
}
