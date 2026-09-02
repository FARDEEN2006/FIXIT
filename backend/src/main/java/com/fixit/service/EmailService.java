package com.fixit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.internet.MimeMessage;

/**
 * Email Service
 * 
 * Handles:
 * - Email verification for second-hand listings
 * - Notifications to admin
 * - Seller communications
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender javaMailSender;

    @Value("${app.name:FIXIT}")
    private String appName;

    @Value("${app.admin-email}")
    private String adminEmail;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Send verification email to seller
     */
    public void sendVerificationEmail(String toEmail, String sellerName, String listingId, String verificationToken) {
        try {
            if (javaMailSender == null) {
                logger.warn("JavaMailSender not configured. Email not sent to: {}", toEmail);
                return;
            }

            String verificationLink = frontendUrl + "/verify-email?token=" + verificationToken + "&listing=" + listingId;
            
            String subject = "Verify Your " + appName + " Listing";
            String htmlContent = buildVerificationEmailHtml(sellerName, verificationLink);

            sendHtmlEmail(toEmail, subject, htmlContent);
            logger.info("Verification email sent to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send verification email: {}", e.getMessage(), e);
        }
    }

    /**
     * Send listing rejection notification
     */
    public void sendListingRejectionEmail(String sellerEmail, String sellerName, String productName, String reason) {
        try {
            if (javaMailSender == null) {
                logger.warn("JavaMailSender not configured. Email not sent to: {}", sellerEmail);
                return;
            }

            String subject = appName + " - Listing Not Approved";
            String htmlContent = buildRejectionEmailHtml(sellerName, productName, reason);

            sendHtmlEmail(sellerEmail, subject, htmlContent);
            logger.info("Rejection email sent to: {}", sellerEmail);
        } catch (Exception e) {
            logger.error("Failed to send rejection email: {}", e.getMessage(), e);
        }
    }

    /**
     * Send listing acceptance notification
     */
    public void sendListingAcceptanceEmail(String sellerEmail, String sellerName, String productName) {
        try {
            if (javaMailSender == null) {
                logger.warn("JavaMailSender not configured. Email not sent to: {}", sellerEmail);
                return;
            }

            String subject = appName + " - Your Listing Has Been Approved!";
            String htmlContent = buildAcceptanceEmailHtml(sellerName, productName);

            sendHtmlEmail(sellerEmail, subject, htmlContent);
            logger.info("Acceptance email sent to: {}", sellerEmail);
        } catch (Exception e) {
            logger.error("Failed to send acceptance email: {}", e.getMessage(), e);
        }
    }

    /**
     * Send admin notification for new enquiry
     */
    public void sendAdminNotificationForEnquiry(String customerName, String productName, String customerPhone) {
        try {
            if (javaMailSender == null) {
                logger.warn("JavaMailSender not configured. Admin notification not sent");
                return;
            }

            String subject = appName + " - New Product Enquiry";
            String htmlContent = buildAdminNotificationHtml(customerName, productName, customerPhone);

            sendHtmlEmail(adminEmail, subject, htmlContent);
            logger.info("Admin notification sent for enquiry from: {}", customerName);
        } catch (Exception e) {
            logger.error("Failed to send admin notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Send HTML email
     */
    private void sendHtmlEmail(String toEmail, String subject, String htmlContent) throws Exception {
        if (javaMailSender == null) {
            logger.warn("JavaMailSender not configured. Email not sent.");
            return;
        }

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        javaMailSender.send(message);
    }

    /**
     * Build verification email HTML
     */
    private String buildVerificationEmailHtml(String sellerName, String verificationLink) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px; }" +
                ".header { text-align: center; margin-bottom: 30px; }" +
                ".button { background-color: #007bff; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block; margin: 20px 0; }" +
                ".footer { text-align: center; margin-top: 30px; font-size: 12px; color: #777; }" +
                "</style></head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'><h2>" + appName + " Email Verification</h2></div>" +
                "<p>Hi " + sellerName + ",</p>" +
                "<p>Thank you for submitting your product listing! To proceed, please verify your email address.</p>" +
                "<a href='" + verificationLink + "' class='button'>Verify Email</a>" +
                "<p>Or copy this link: <br>" + verificationLink + "</p>" +
                "<p>This link expires in 24 hours.</p>" +
                "<p>Best regards,<br>The " + appName + " Team</p>" +
                "<div class='footer'><p>This is an automated email. Please do not reply.</p></div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * Build rejection email HTML
     */
    private String buildRejectionEmailHtml(String sellerName, String productName, String reason) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px; }" +
                ".header { text-align: center; margin-bottom: 30px; }" +
                ".alert { background-color: #fff3cd; border: 1px solid #ffc107; padding: 15px; border-radius: 5px; margin: 20px 0; }" +
                ".footer { text-align: center; margin-top: 30px; font-size: 12px; color: #777; }" +
                "</style></head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'><h2>" + appName + " - Listing Update</h2></div>" +
                "<p>Hi " + sellerName + ",</p>" +
                "<p>Thank you for submitting your listing for <strong>" + productName + "</strong>.</p>" +
                "<div class='alert'>" +
                "<p><strong>Update:</strong> Unfortunately, your listing could not be approved at this time.</p>" +
                "<p><strong>Reason:</strong> " + reason + "</p>" +
                "</div>" +
                "<p>You can submit another listing if you'd like. Please contact us if you have any questions.</p>" +
                "<p>Best regards,<br>The " + appName + " Team</p>" +
                "<div class='footer'><p>This is an automated email. Please do not reply.</p></div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * Build acceptance email HTML
     */
    private String buildAcceptanceEmailHtml(String sellerName, String productName) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px; }" +
                ".header { text-align: center; margin-bottom: 30px; }" +
                ".success { background-color: #d4edda; border: 1px solid #28a745; padding: 15px; border-radius: 5px; margin: 20px 0; }" +
                ".footer { text-align: center; margin-top: 30px; font-size: 12px; color: #777; }" +
                "</style></head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'><h2>" + appName + " - Listing Approved!</h2></div>" +
                "<p>Hi " + sellerName + ",</p>" +
                "<div class='success'>" +
                "<p><strong>Great news!</strong> Your listing for <strong>" + productName + "</strong> has been approved.</p>" +
                "</div>" +
                "<p>We will be in touch with you soon. Thank you for using " + appName + "!</p>" +
                "<p>Best regards,<br>The " + appName + " Team</p>" +
                "<div class='footer'><p>This is an automated email. Please do not reply.</p></div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * Build admin notification HTML
     */
    private String buildAdminNotificationHtml(String customerName, String productName, String customerPhone) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px; }" +
                ".header { text-align: center; margin-bottom: 30px; background-color: #f8f9fa; padding: 15px; }" +
                ".info-box { background-color: #e7f3ff; border-left: 4px solid #2196F3; padding: 15px; margin: 15px 0; }" +
                ".footer { text-align: center; margin-top: 30px; font-size: 12px; color: #777; }" +
                "</style></head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'><h2>New Product Enquiry - Action Required</h2></div>" +
                "<p>A customer has enquired about your product.</p>" +
                "<div class='info-box'>" +
                "<p><strong>Product:</strong> " + productName + "</p>" +
                "<p><strong>Customer Name:</strong> " + customerName + "</p>" +
                "<p><strong>Customer Phone:</strong> " + customerPhone + "</p>" +
                "</div>" +
                "<p>Please follow up with the customer as soon as possible via WhatsApp or phone.</p>" +
                "<p>Log in to your " + appName + " admin dashboard to view more details and manage enquiries.</p>" +
                "<div class='footer'><p>This is an automated notification. Please do not reply.</p></div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
